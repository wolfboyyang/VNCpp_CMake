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
package es.farfuteam.vncpp.model;

/**
 * @interface ObserverCanvas
 * @brief This is the Observer interface for Canvas
 * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
 * @details This is the Observer interface for Canvas
 */
public interface ObserverCanvas {
    public void updateIniFrame(int[] data, int offset, int x, int y, int width, int height);

    public void updateRedraw();

    public void updateFinish();

    public String updatePass();

    public void updateOutOfMemory();
}
