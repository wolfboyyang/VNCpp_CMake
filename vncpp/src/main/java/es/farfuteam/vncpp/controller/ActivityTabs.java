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
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import es.farfuteam.vncpp.controller.NewConnectionActivity.QualityArray;
import es.farfuteam.vncpp.model.sql.Connection;
import es.farfuteam.vncpp.model.sql.ConnectionSQLite;
import es.farfuteam.vncpp.view.Adapter;
import es.farfuteam.vncpp.view.DialogOptions.SuperListener;
import es.farfuteam.vncpp.view.ListFragmentTab;
import es.farfuteam.vncpp.view.ListFragmentTabFav;


/**
 * @class ClientActivityTabs
 * @brief This is the main activity.
 * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
 * @extends FragmentActivity
 * @implements SuperListener
 */

public class ActivityTabs extends FragmentActivity implements SuperListener {

    /**
     * @enum InfoDialogs
     * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
     * @details Dialogs with util info for the user
     */
    public enum InfoDialogs {
        infoDialog,///< Info dialog
        createNonConnectionDialog,///< Connection no available dialog
        exitDialog,///< Exit dialog
        aboutDialog
    }

    ;///< About dialog

    /**
     * Preferences file
     */
    public static final String PREFS_NAME = "PreferencesFile";

    /**
     * A string
     */
    private static String ACTIVE_TAB = "activeTab";

    /**
     * Object
     */
    private Object o;

    /**
     * Adapter of the list
     */
    private Adapter adapter;


    /**
     * @param savedInstanceState
     * @brief This is the onCreate method
     * @details The onCreate method adds an actionBar to the activity with two tabs (recent and favorites).
     * It also load the preferences file into the prefs attribute and sets the rememeberExit attribute.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_host);


        final ActionBar actionBar = getActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);


        final String recents = getString(R.string.recents);
        final String favorites = getString(R.string.favoritesTab);

        // add tabs
        Tab tab1 = actionBar.newTab()
                .setText(recents)
                .setTabListener(new TabListener<ListFragmentTab>(
                        this, "tab1", ListFragmentTab.class));
        actionBar.addTab(tab1);


        Tab tab2 = actionBar.newTab()
                .setText(favorites)
                .setTabListener(new TabListener<ListFragmentTabFav>(
                        this, "tab2", ListFragmentTabFav.class));

        actionBar.addTab(tab2);


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //accedemos a fichero preferencias
        (Configuration.getInstance()).setPrefs(getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE));


        (Configuration.getInstance()).readPrefs();

        // Orientation Change Occurred
        if (savedInstanceState != null) {
            int currentTabIndex = savedInstanceState.getInt("tab_index");
            actionBar.setSelectedNavigationItem(currentTabIndex);
        }

        //nombre en la activity bar
        final String title = getString(R.string.connections);
        setTitle(title);

    }

    /**
     * @param menu
     * @return always true
     * @brief Adds the new connection item to the top bar
     * @details The onCreateOptionsMenu adds the new connection item to the action bar created before on
     * the onCreate method
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_connection, menu);
        return true;
    }

    /**
     * @param item
     * @return always true
     * @brief Handles the item selection
     * @details Handles the item selection. Not ready yet
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addConnection:
                Intent i = new Intent(this, NewConnectionActivity.class);
                startActivity(i);
                return true;

            case R.id.configuration:
                Intent iConf = new Intent(this, ConfigurationMenu.class);
                startActivity(iConf);
                return true;

            case R.id.howto:
                Intent ihelp = new Intent(this, HelpActivity.class);
                startActivity(ihelp);
                return true;

            case R.id.about:
                showDialog(InfoDialogs.aboutDialog.ordinal());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * @param outState
     * @brief Saves the active tab
     * @details Saves the active tab
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save active tab
        outState.putInt(ACTIVE_TAB,
                getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    /**
     * @brief Deletes a connection from the Data Base
     * @details Deletes a connection from the Data Base
     */
    public void deleting() {

        ConnectionSQLite admin = ConnectionSQLite.getInstance(this);
        admin.deleteUser((Connection) getO());

        adapter = new Adapter(this, admin.getAllUsers());

    }

    /**
     * @brief Creates the edit frame
     * @details Creates the edit frame
     */
    public void edit() {

        //Ventana de edition
        Intent intent = new Intent(this, EditionActivity.class);

        intent.putExtra("Name", ((Connection) getO()).getName());
        intent.putExtra("IP", ((Connection) getO()).getIP());
        intent.putExtra("PORT", ((Connection) getO()).getPORT());
        intent.putExtra("PSW", ((Connection) getO()).getPsw());
        intent.putExtra("quality", ((Connection) getO()).getColorFormat().toString());
        intent.putExtra("fav", ((Connection) getO()).isFav());
        //1234 es el codigo que servira para su identificacion en onActivityResult
        startActivityForResult(intent, 1234);

    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     * @brief Saves the information on the Data Base
     * @details If the resultCode is RESULT_OK then saves the information on the Data Base
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1234) {

            if (resultCode == RESULT_OK) {

                String ip = data.getStringExtra("newIP");
                String port = data.getStringExtra("newPORT");
                String psw = data.getStringExtra("newPSW");
                int color = data.getIntExtra("newColor", 0);
                boolean fav = data.getBooleanExtra("fav", false);

                ConnectionSQLite admin = ConnectionSQLite.getInstance(this);

                //seteo valores nuevos
                ((Connection) getO()).setIP(ip);
                ((Connection) getO()).setPORT(port);
                ((Connection) getO()).setPsw(psw);
                ((Connection) getO()).setFav(fav);
                ((Connection) getO()).setColorFormat(QualityArray.values()[color]);

                admin.updateUser((Connection) getO());


            }

        }


    }//onActivityResult


    /**
     * @param view
     * @brief Starts a connection
     * @details This method is called from the android:onClic of the element_user.xml.
     * When it is called calls the connect method which properly makes the connection.
     */
    //tiene que ser publico porque se llama desde el android:onClic del xml
    public void onCheckboxConnectClicked(View view) {

        CheckBox cb = (CheckBox) view;

        //Connection
        setO(cb.getTag());

        connect();

    }

    /**
     * @param view
     * @brief Saves a connection to favorites
     * @details Saves a connection to favorites. It's called from the android:onClic of the
     * element_user.xml
     */
    public void onCheckboxFavClicked(View view) {

        CheckBox cb = (CheckBox) view;
        //User
        setO(cb.getTag());

        boolean fav = ((Connection) getO()).isFav();

        if (fav) {
            ((Connection) getO()).setFav(false);
        } else {
            ((Connection) getO()).setFav(true);
        }

    }

    /**
     * @return o
     * @brief Returns the o attribute
     * @details Returns the o attribute
     */
    public Object getO() {
        return o;
    }

    /**
     * @param o
     * @brief Sets the o attribute
     * @details Sets the o attribute
     */
    public void setO(Object o) {
        this.o = o;
    }

    /**
     * @brief Calls to the deleting method
     * @details Calls to the deleting method
     */
    @Override
    public void deleteUser() {
        deleting();
    }

    /**
     * @brief Calls to the edit method
     * @details Calls to the edit method
     */
    @Override
    public void editingUser() {
        edit();
    }

    /**
     * @brief Starts the canvasActivity
     * @details Starts the canvasActivity
     */
    //la llamo desde dialogo, tiene que ser publica
    public void connect() {

        Intent canvasActivity = new Intent(this, CanvasActivity.class);

        String ip = ((Connection) getO()).getIP();
        String port = ((Connection) getO()).getPORT();
        String psw = ((Connection) getO()).getPsw();
        QualityArray color = ((Connection) getO()).getColorFormat();

        canvasActivity.putExtra("ip", ip);
        canvasActivity.putExtra("port", port);
        canvasActivity.putExtra("psw", psw);
        canvasActivity.putExtra("color", color.toString());


        //Aquí veo el tipo de conexión, para usar un tipo de compresión de imagen u otro
        if (checkConnectivity()) {

            canvasActivity.putExtra("wifi", isWifiConnectivityType());
            startActivity(canvasActivity);

        } else {
            //dialogo alerta No conexion habilitada
            showDialog(InfoDialogs.createNonConnectionDialog.ordinal());
        }


    }

    /**
     * @brief Shows the dialog with the connection information
     * @details Shows the dialog with the connection information
     */
    public void showInfoDialog() {
        showDialog(InfoDialogs.infoDialog.ordinal());
    }

    /**
     * @param keyCode
     * @param event
     * @return True if the event is handled properly. If the keyCode is not equal to KEYCODE_BACK
     * it returns the event.
     * @brief Handles the onKeyDown event
     * @details Only handles the back key. Otherwise it returns the event. When the back key is
     * down and the rememberExit is equal to true then finishes the activity. If the rememberExit
     * is equal to false
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //si en las preferencias esta a true, se sale sin preguntar
            if ((Configuration.getInstance()).isRememberExit()) {

                finish();
            } else {

                showDialog(InfoDialogs.exitDialog.ordinal());

            }

            // Si el listener devuelve true, significa que el evento esta procesado, y nadie debe hacer nada mas
            return true;
        }


        //para las demas cosas, se reenvia el evento al listener habitual
        return super.onKeyDown(keyCode, event);

    }//onKeyDown


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
     * @param id the dialog ID
     * @return Dialog created
     * @brief Override function to create dialogs
     * @details Creates the dialog with a showDialog(id) called,
     * id is the number of the dialog to be created
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case 0:
                dialog = infoDialog();
                break;
            case 1:
                dialog = createNonConnectionDialog();
                break;
            case 2:
                dialog = createExitDialog();
                break;
            case 3:
                dialog = createAboutDialog();
                break;
        }

        return dialog;
    }

    /**
     * @return The new dialog
     * @brief Shows the dialog with the exit question
     * @details Shows the dialog with the exit question
     */
    private Dialog createExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View checkBoxOut = View.inflate(this, R.layout.checkbox_out, null);
        final CheckBox checkBox = (CheckBox) checkBoxOut.findViewById(R.id.checkboxOut);

        final String decision = getString(R.string.RememberCheckBox);

        checkBox.setText(decision);

        final String titleExit = getString(R.string.DialogTitleExit);
        final String question = getString(R.string.DialogQuestion);
        final Activity actThis = this;
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(titleExit);
        builder.setMessage(question);
        builder.setView(checkBoxOut);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                actThis.removeDialog(InfoDialogs.exitDialog.ordinal());

            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {//un listener que al pulsar, cierre la aplicacion
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Salir
                (Configuration.getInstance()).setRememberExit(checkBox.isChecked());
                finish();
            }
        });
        return builder.create();
    }

    /**
     * @return The new dialog
     * @brief Shows the dialog to indicate there is no network connection
     * @details Shows the dialog to indicate there is no network connection
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
     * @return The new dialog
     * @brief Shows the dialog to indicate the connection info
     * @details Shows the dialog to indicate the connection info
     */
    private Dialog infoDialog() {

        final Activity actThis = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String portText = getString(R.string.port);
        final String qualityText = getString(R.string.Quality);
        final String connectionText = getString(R.string.connection);

        String name = ((Connection) getO()).getName();
        String ip = ((Connection) getO()).getIP();
        String port = ((Connection) getO()).getPORT();
        QualityArray color = ((Connection) getO()).getColorFormat();

        String quality = messageQuality(color);

        builder.setMessage("IP: " + ip + "\n" + portText + ": " + port + "\n" + qualityText + ": " + quality);

        builder.setTitle(connectionText + " " + name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                actThis.removeDialog(InfoDialogs.infoDialog.ordinal());
            }

        });

        return builder.create();

    }

    /**
     * @param color quality image
     * @return String quality
     * @brief Receives the QualityArray and return the String
     * @details Receives the QualityArray and return the String to show the info in String type
     */
    private String messageQuality(QualityArray color) {

        final String[] colors = getResources().getStringArray(R.array.color_array);

        int position = color.ordinal();

        switch (position) {
            case 0:
                //SuperHigh
                return colors[0];
            case 1:
                //High
                return colors[1];
            case 2:
                //Medium
                return colors[2];
            case 3:
                //Low
                return colors[3];

        }

        return null;

    }

    /**
     * @return The new dialog
     * @brief Shows the dialog to indicate about info
     * @details Shows the dialog to indicate about info
     */
    private Dialog createAboutDialog() {
        //necesario para poder clicar en los links
        final TextView message = new TextView(this);
        final SpannableString s =
                new SpannableString(this.getText(R.string.about_message));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        return new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setView(message)
                .setPositiveButton(R.string.about_ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Auto-generated method stub

                            }
                        }
                )
                .show();
    }


}

