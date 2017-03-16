/*
 	Copyright 2013 Oscar Crespo Salazar
 	Copyright 2013 Gorka Jimeno Garrachon
 	Copyright 2013 Luis Valero Martin
  
	This file is part of VNCpp.

	VNCpp is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	any later version.
	
	VNCpp is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with VNCpp.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.farfuteam.vncpp.controller;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import es.farfuteam.vncpp.model.sql.Connection;
import es.farfuteam.vncpp.model.sql.ConnectionSQLite;


/**
 * @class NewConnectionActivity
 * @brief This is the activity created to make a new connection
 * @extends FragmentActivity
 * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
 */
public class NewConnectionActivity extends FragmentActivity {

    /**
     * @enum QualityArray
     * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
     * @details Controls the image quality
     */
    public enum QualityArray {
        SuperHigh, High, Medium, Low
    }

    ;

    /**
     * EditText to write the name connection
     */
    private EditText ConnectionName_field;
    /**
     * EditText to write the ip connection
     */
    private EditText IP_field;
    /**
     * EditText to write the port connection
     */
    private EditText PORT_field;
    /**
     * EditText to write the password connection
     */
    private EditText PSW_field;

    /**
     * connection name
     */
    private String connectionName;
    /**
     * connection ip
     */
    private String IP;
    /**
     * connection port
     */
    private String PORT;
    /**
     * connection psw
     */
    private String PSW;
    /**
     * connection pswAuth
     */
    private String PSWAuth;
    /**
     * spinners with the quality image
     */
    private Spinner Spinner_colors;
    /**
     * image quality selected
     */
    private QualityArray color_format;


    /**
     * @param savedInstanceState
     * @brief This is the onCreate method
     * @details The onCreate method adds buttons and edittext on the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.connection_window);

        ConnectionName_field = (EditText) findViewById(R.id.ConnectionName_inserted);

        IP_field = (EditText) findViewById(R.id.IP_inserted);

        PORT_field = (EditText) findViewById(R.id.PORT_inserted);

        PSW_field = (EditText) findViewById(R.id.PSW_inserted);

        //desplegable seleccion de colores

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.color_array,
                        android.R.layout.simple_spinner_item);


        Spinner_colors = (Spinner) findViewById(R.id.Spinner_colors);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        Spinner_colors.setAdapter(adapter);

        Spinner_colors.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               android.view.View v, int position, long id) {
                        setColor_format(getPosEnumQuality(position));
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //por defecto se selecciona la posicion 0, 24-bit color(extra-high)
                        setColor_format(QualityArray.SuperHigh);
                    }
                });


        Button botonConnect = (Button) findViewById(R.id.buttonConnect);
        botonConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                //primero se verifica que los campos se rellenan bien,
                //y se añade a la BD antes de iniciar Canvas.

                if (verify(v)) {

                    //crear usuario si todo ha ido bien
                    createNewConnection();

                    iniCanvasActivity();

                }

            }
        });


        Button botonCancel = (Button) findViewById(R.id.buttonCancel);
        botonCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Cancel(v);

            }
        });

        //efectos del actionBar
        final ActionBar actionBar = getActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);

    }

    /**
     * @param pos position on the array
     * @return the image quality
     * @brief Returns the image quality
     * @details Returns the image quality of th enum
     */
    private QualityArray getPosEnumQuality(int pos) {

        switch (pos) {

            case 0:
                return QualityArray.SuperHigh;
            case 1:
                return QualityArray.High;
            case 2:
                return QualityArray.Medium;
            case 3:
                return QualityArray.Low;
            default:
                break;

        }
        return null;
    }

    /**
     * @brief Method that initialized the Canvas Activity
     * @details Method that initialized the Canvas Activity
     */
    private void iniCanvasActivity() {

        Intent canvasActivity = new Intent(this, CanvasActivity.class);

        canvasActivity.putExtra("ip", getIP());
        canvasActivity.putExtra("port", getPORT());
        canvasActivity.putExtra("psw", getPSWAuth());
        canvasActivity.putExtra("color", getColor_format().toString());

        //Aquí veo el tipo de conexión, para usar un tipo de compresión de imagen u otro
        if (checkConnectivity()) {

            canvasActivity.putExtra("wifi", isWifiConnectivityType());

            startActivity(canvasActivity);

            //se finaliza activity
            finish();

        } else {
            //dialogo alerta No conexion habilitada
            showDialog(2);
        }


    }

    /**
     * @param v the view
     * @brief Cancels the creation of new connection
     * @details Cancels the creation of new connection
     */
    private void Cancel(View v) {
        finish();
    }

    /**
     * @return True if the connectivity exists, false in another case.
     * @brief Checks the connectivity of the terminal
     * @details Checks the connectivity of the terminal
     */
    private boolean checkConnectivity() {
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable())) {
            enabled = false;
        }
        return enabled;
    }

    /**
     * @return True if the connectivity type is Wifi, false in another case.
     * @brief Checks the connectivity type of the terminal
     * @details Checks the connectivity type of the terminal
     */
    //devuelve true si es conexion wifi, false en caso contrario
    private boolean isWifiConnectivityType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        String connectionType = info.getTypeName();

        if (connectionType.equalsIgnoreCase("wifi")) {
            return true;
        } else {
            //3g u otro tipo
            return false;
        }

    }


    /**
     * @param newConfig the new state
     * @brief Preserved the state of the activity
     * @details Preserved the state of the activity when the terminal changed the orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * @param item
     * @return always true
     * @brief Handles the item selection
     * @details Handles the item selection. Not ready yet
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Sirve para volver a Tabs al pulsar en la actionBar
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * @param v the view
     * @brief Verifies the data introduced by the user
     * @details Verifies the data introduced by the user
     */
    private boolean verify(View v) {

        //Comprobar validez IP
        IP = IP_field.getText().toString();


        if (IP.equals("") || !validateIPAddress(IP)) {
            final String invalidIp = getString(R.string.invalidIp);
            Toast.makeText(this, invalidIp, Toast.LENGTH_SHORT).show();
            return false;
        }

        //Comprobar validez Puerto
        PORT = PORT_field.getText().toString();

        if (PORT.equals("") || !validPort(PORT)) {
            final String invalidPort = getString(R.string.invalidPort);
            Toast.makeText(this, invalidPort, Toast.LENGTH_SHORT).show();
            return false;
        }

        //Comprobar nombre de conexion no repetido
        connectionName = ConnectionName_field.getText().toString();

        if (connectionName.equals("") || !validNameConnection(connectionName)) {
            final String invalidName = getString(R.string.invalidName);
            Toast.makeText(this, invalidName, Toast.LENGTH_SHORT).show();
            return false;
        }

        setPSWAuth(PSW_field.getText().toString());

        return true;

    }

    /**
     * @param name the name of the connection
     * @return true if is a valid name, false if this name is in use
     * @brief Valids the connection name
     * @details Returns true if this name it is not in use
     */
    private boolean validNameConnection(String name) {

        //se mira en la base de datos que no exista
        ConnectionSQLite dataBase = ConnectionSQLite.getInstance(this);
        if (dataBase.searchNameConnection(name)) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * @return true if it is a valid Port, false in another case
     * @brief This function checks the port parameter
     * @details This function checks the port parameter
     */
    private boolean validPort(String port) {

        try {
            int p = Integer.parseInt(port);
            //rango de puertos no aceptados
            if ((p < 0) || (p > 65535)) {
                return false;
            }
        } catch (NumberFormatException s) {
            return false;
        }
        return true;
    }

    /**
     * @param ipAddress the address
     * @return true if it is a valid IP, false in another case
     * @brief This function checks the ip format
     * @details This function checks the ip format
     */
    private boolean validateIPAddress(String ipAddress) {

        String[] tokens = ipAddress.split("\\.");

        if (tokens.length != 4) {
            return false;
        }

        for (String str : tokens) {

            int i;

            try {
                i = Integer.parseInt(str);
            } catch (NumberFormatException s) {
                return false;
            }

            if ((i < 0) || (i > 255)) {
                return false;
            }

        }

        return true;
    }


    /**
     * @brief Creates and connects to the new connection
     * @details Creates and connects to the new connection
     */
    private void createNewConnection() {

        if (!isEmpty(ConnectionName_field) && !isEmpty(IP_field) && !isEmpty(PORT_field)) {


            connectionName = ConnectionName_field.getText().toString();
            IP = IP_field.getText().toString();
            PORT = PORT_field.getText().toString();
            PSW = PSW_field.getText().toString();

            Connection c = new Connection(connectionName, IP, PORT, PSW, false, getColor_format());

            //se anade el usuario a la base de datos
            ConnectionSQLite dataBase = ConnectionSQLite.getInstance(this);
            dataBase.newUser(c);

        } else {
            //Dialogo alerta
            showDialog(1);
        }
    }

    /**
     * @param id
     * @return Dialog created
     * @brief Override function to create dialogs
     * @details Creates the dialog with a showDialog(id) called,
     * id is the number of the dialog to be created
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case 1:
                dialog = createAlertDialog();
                break;
            case 2:
                dialog = createNonConnectionDialog();
                break;
            default:
                dialog = createAlertDialog();
                break;
        }

        return dialog;
    }

    /**
     * @return The new dialog
     * @brief Shows the dialog when any field is empty
     * @details Shows the dialog when any field is empty
     */
    private Dialog createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String info = getString(R.string.DialogInfo);
        String someEmpty = getString(R.string.DialogSomethingEmpty);

        builder.setTitle(info);
        builder.setMessage(someEmpty);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });

        return builder.create();
    }

    /**
     * @return The new dialog
     * @brief Shows the dialog when the connection is not available
     * @details Shows the dialog when the connection is not available
     */
    private Dialog createNonConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String info = getString(R.string.DialogNonConnectionInfo);
        String someEmpty = getString(R.string.DialogNonConnection);

        builder.setTitle(info);
        builder.setMessage(someEmpty);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });

        return builder.create();
    }

    /**
     * @return False if the EditText is empty,true in another case
     * @brief Controls if the EditText is empty
     * @details Controls if the EditText is empty
     */
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param keyCode
     * @param event
     * @return True if the event is handled properly. If the keyCode is not equal to KEYCODE_BACK
     * it returns the event
     * @brief Handles the onKeyDown event
     * @details Only handles the back key. Otherwise it returns the event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            final String titleExit = getString(R.string.DialogTitleExit);
            final String question = getString(R.string.DialogQuestion);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(titleExit)
                    .setMessage(question)
                    .setNegativeButton(android.R.string.cancel, null)//sin listener
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {//un listener que al pulsar, cierre la aplicacion
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Salir
                            finish();
                        }
                    })
                    .show();

            return true;

        }

        //para las demas cosas, se reenvia el evento al listener habitual
        return super.onKeyDown(keyCode, event);

    }

    /**
     * @return connectionName the connection name
     * @brief Returns the connectionName attribute
     * @details Returns the connectionName attribute
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * @param connectionName the connection name
     * @brief Sets the connectionName attribute
     * @details Sets the connectionName attribute
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * @return IP
     * @brief Returns the IP attribute
     * @details Returns the IP attribute
     */
    public String getIP() {
        return IP;
    }

    /**
     * @param iP
     * @brief Sets the IP attribute
     * @details Sets the IP attribute
     */
    public void setIP(String iP) {
        IP = iP;
    }

    /**
     * @return PSW the password atributte
     * @brief Returns the PSW attribute
     * @details Returns the PSW attribute
     */
    public String getPSW() {
        return PSW;
    }

    /**
     * @param pSW the password of the connection
     * @brief Sets the PSW attribute
     * @details Sets the PSW attribute
     */
    public void setPSW(String pSW) {
        PSW = pSW;
    }

    /**
     * @return PORT
     * @brief Returns the PORT attribute
     * @details Returns the PORT attribute
     */
    public String getPORT() {
        return PORT;
    }

    /**
     * @param pSW the password of the connection
     * @brief Sets the PSW attribute
     * @details Sets the PSW attribute
     */
    public void setPORT(String pORT) {
        PORT = pORT;
    }

    /**
     * @return color_format
     * @brief Returns the color_format attribute
     * @details Returns the color_format attribute
     */
    public QualityArray getColor_format() {
        return color_format;
    }

    /**
     * @param color_format the image quality of the connection
     * @brief Sets the color_format attribute
     * @details Sets the color_format attribute
     */
    public void setColor_format(QualityArray color_format) {
        this.color_format = color_format;
    }

    /**
     * @return PSWAuth
     * @brief Returns the PSWAuth attribute
     * @details Returns the PSWAuth attribute
     */
    public String getPSWAuth() {
        return PSWAuth;
    }

    /**
     * @param pSWAuth the password authentication of the connection
     * @brief Sets the PSWAuth attribute
     * @details Sets the PSWAuth attribute
     */
    public void setPSWAuth(String pSWAuth) {
        PSWAuth = pSWAuth;
    }


}
