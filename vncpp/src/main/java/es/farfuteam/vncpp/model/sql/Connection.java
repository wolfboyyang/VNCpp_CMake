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
package es.farfuteam.vncpp.model.sql;

import es.farfuteam.vncpp.controller.NewConnectionActivity.QualityArray;

/**
 * @class Connection
 * @brief This is the class which stores the connections
 * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
 */
public class Connection {

    /**
     * name connection
     */
    private String name;
    /**
     * ip connection
     */
    private String IP;
    /**
     * port connection
     */
    private String PORT;
    /**
     * password connection
     */
    private String psw;
    /**
     * favorites connection
     */
    private boolean fav;
    /**
     * image quality connection
     */
    private QualityArray ColorFormat;

    /**
     * @brief Default constructor
     * @details Default constructor
     */
    public Connection() {
        name = "default";
        IP = "192.168.1.1";
        PORT = "5900";
        psw = "";
        fav = false;
        //ColorFormat="24-bit color (4 bpp)";
    }

    /**
     * @param name        Name connection
     * @param IP          IP connection
     * @param PORT        Port connection
     * @param psw         Password connection
     * @param fav         Fav connection
     * @param ColorFormat Image quality
     * @brief Connection constructor
     */
    public Connection(String name, String IP, String PORT, String psw, boolean fav, QualityArray ColorFormat) {
        this.setName(name);
        this.setIP(IP);
        this.setPORT(PORT);
        this.setPsw(psw);
        this.setFav(fav);
        this.setColorFormat(ColorFormat);
    }

    /**
     * @return name the name attribute
     * @brief Returns the name attribute
     * @details Returns the name attribute
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name of the connection
     * @brief Sets the name attribute
     * @details Sets the name attribute
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return IP the IP atributte
     * @brief Returns the IP attribute
     * @details Returns the IP attribute
     */
    public String getIP() {
        return IP;
    }

    /**
     * @param iP the iP of the connection
     * @brief Sets the iP attribute
     * @details Sets the iP attribute
     */
    public void setIP(String iP) {
        IP = iP;
    }

    /**
     * @return port the port atributte
     * @brief Returns the Port attribute
     * @details Returns the port attribute
     */
    public String getPORT() {
        return PORT;
    }

    /**
     * @param pORT the port of the connection
     * @brief Sets the port attribute
     * @details Sets the port attribute
     */
    public void setPORT(String pORT) {
        PORT = pORT;
    }

    /**
     * @return PSW the password atributte
     * @brief Returns the PSW attribute
     * @details Returns the PSW attribute
     */
    public String getPsw() {
        return psw;
    }

    /**
     * @param pSW the password of the connection
     * @brief Sets the PSW attribute
     * @details Sets the PSW attribute
     */
    public void setPsw(String psw) {
        this.psw = psw;
    }

    /**
     * @return fav the fav atributte
     * @brief Returns the fav attribute
     * @details Returns the fav attribute
     */
    public boolean isFav() {
        return fav;
    }

    /**
     * @param fav the fav of the connection
     * @brief Sets the fav attribute
     * @details Sets the fav attribute
     */
    public void setFav(boolean fav) {
        this.fav = fav;
    }

    /**
     * @return ColorFormat the ColorFormat atributte
     * @brief Returns the ColorFormat attribute
     * @details Returns the ColorFormat attribute
     */
    public QualityArray getColorFormat() {
        return ColorFormat;
    }

    /**
     * @param colorFormat the colorFormat of the connection
     * @brief Sets the colorFormat attribute
     * @details Sets the colorFormat attribute
     */
    public void setColorFormat(QualityArray colorFormat) {
        ColorFormat = colorFormat;
    }


}
