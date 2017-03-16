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
#include "ClientConnectionRFB.h"

#include <jni.h>
#include <android/log.h>


#include "HandlerRFB.h"


#define bitsPerSample 8
#define samplesPerPixel 3
#define bytesPerPixel 4
#define timeWait 500

/**
 * @brief The default constructor
 * @details The default constructor. Initializes the clientRFB as NULL and stop_connection as false
 */
ClientConnectionRFB::ClientConnectionRFB() {
    //cl = NULL;
    //clientRFB=rfbGetClient(8,3,4);
    clientRFB = NULL;
    stop_connection = false;
    thread_finish = true;

    //buttonMask = 0;

}

/**
 * @brief The default destroyer
 * @details The default destroyer. Sets stop_connection as true, stops the thread and frees the connection information
 */
ClientConnectionRFB::~ClientConnectionRFB() {
    if (DEBUG) {
        LOGE("Limpianndo rfb");
    }
    stop_connection = true;

    while (thread_finish == false) {
        sleep(1);
    }

    cleanRfb();

    /*if(clientRFB != NULL){
        rfbClientCleanup(clientRFB);

        delete clientRFB;
        clientRFB = NULL;
    }*/

}

/**
 * @brief Starts the connection
 * @param host The IP
 * @param port The port
 * @param pass The password
 * @param picture_quality The image quality
 * @param compress The image has to be compress or not
 * @param hide_mouse The mouse has to be hide or not
 * @return ALLOK if everything ok, or an error
 * @details Starts the connection with the server. Configures all the functions (handler) that have to be invoke in RFB events.
 * When the connection is completed starts the main thread with the function eventLoop
 */
ConnectionError
ClientConnectionRFB::iniConnection(char *host, int port, char *pass, int picture_quality,
                                   int compress, bool hide_mouse) {
    if (DEBUG)
        LOGE("JNI Iniciando conexion");


    clientRFB = rfbGetClient(bitsPerSample, samplesPerPixel, bytesPerPixel);

    clientRFB->serverPort = port;
    clientRFB->serverHost = host;

    clientRFB->programName = "VNC++";

    HandlerRFB::setPass(pass);

    clientRFB->GetPassword = HandlerRFB::getPass;

    clientRFB->appData.qualityLevel = picture_quality;
    clientRFB->appData.compressLevel = compress;
    clientRFB->appData.useRemoteCursor = hide_mouse;
    //clientRFB->appData.useRemoteCursor = false;

    clientRFB->MallocFrameBuffer = HandlerRFB::iniFrameBuffer;
    clientRFB->canHandleNewFBSize = TRUE;
    clientRFB->GotFrameBufferUpdate = HandlerRFB::updateScreen;
    clientRFB->FinishedFrameBufferUpdate = HandlerRFB::finishUpdate;
    //clientRFB->HandleKeyboardLedState=kbd_leds;
    //clientRFB->HandleTextChat=text_chat;
    //clientRFB->GotXCutText = got_selection;
    clientRFB->listenPort = LISTEN_PORT_OFFSET;
    clientRFB->listen6Port = LISTEN_PORT_OFFSET;

    ConnectionError error_connect;

    if (!rfbInitClient(clientRFB, 0, NULL)) {
        error_connect = NoServerFound;
        if (DEBUG)
            LOGE("No server found");
        clientRFB = NULL;
    } else if (!clientRFB->frameBuffer) {
        if (DEBUG)
            LOGE("No Frame Found");
        error_connect = NoFrameFound;
        cleanRfb();

    }

    if (error_connect == NoFrameFound || error_connect == NoServerFound) {
        return error_connect;
    }
    stop_connection = false;

    int error_thread;

    error_thread = pthread_create(&mainThreadId, NULL, eventLoop, this);
    if (error_thread) {
        if (DEBUG)
            LOGE("Error create thread");
        error_connect = errorCreateThread;
    }

    error_connect = ALLOK;
    //si hubo un error se finaliza.
    if (error_connect != ALLOK) {
        stop_connection = true;
        cleanRfb();
    } else {
        pthread_detach(mainThreadId);
    }
    //eventLoop(this);


    if (DEBUG)
        LOGE("Inicio de conexion OK");

    return error_connect;
}

/**
 * @brief Frees all the clientRFB information
 * @details Frees all the clientRFB information. If there are a current connection it will be closed
 */
void ClientConnectionRFB::cleanRfb() {
    if (DEBUG)
        LOGE("Close connection");


    if (clientRFB != NULL) {

        close(clientRFB->sock);
        /*if(!clientRFB->listenSpecified){
            free((void*)clientRFB->appData.encodingsString);
        }*/
        if (clientRFB->frameBuffer) {
            free(clientRFB->frameBuffer);
            clientRFB->frameBuffer = NULL;
        }

        rfbClientCleanup(clientRFB);
        if (DEBUG)
            LOGE("Fin Limpiando Rfb");
        //delete clientRFB;


        clientRFB = NULL;
    }
}

/**
 * @brief The RFB main loop
 * @param This A pointer to the ClientConnectionRFB object
 * @details This loop handles all the RFB events. It wont stop until the server stops the connection or the client sends the event
 */
void *ClientConnectionRFB::eventLoop(void *This) {

    int mes;
    ClientConnectionRFB *aux_this = (ClientConnectionRFB *) This;
    if (DEBUG)
        LOGE("LOOP");
    aux_this->thread_finish = false;
    bool serverOut = false;
    while (!aux_this->stop_connection) {
        mes = WaitForMessage(aux_this->clientRFB, timeWait);

        if (mes < 0) {
            aux_this->stop_connection = true;
            serverOut = true;
        }
        if (mes) {
            if (!HandleRFBServerMessage(aux_this->clientRFB)) {
                aux_this->stop_connection = true;
                serverOut = true;
            }
        }
    }

    //aux_this->cleanRfb();
    if (DEBUG)
        LOGE("Fin cleanRfb");


    if (serverOut == true) {
        HandlerRFB::finishConnection();
    } else {
        if (DEBUG)
            LOGE("JNI FinishClient");
        HandlerRFB::finishClient();
    }
    aux_this->thread_finish = true;
    //pthread_detach(pthread_self());
    //pthread_exit(NULL);
}

/**
 * @brief Sets stop_connection as true
 * @details Sets stop_connection as true
 */
void ClientConnectionRFB::stopConnection() {
    stop_connection = true;
}

/**
 * @brief Sends a mouse event
 * @param x The x coordinate
 * @param y the y coordinate
 * @param event The mouse event
 * @return true if everything ok, otherwise false
 * @details Sends a mouse envet to the server
 */
bool ClientConnectionRFB::sendMouseEvent(int x, int y, MouseEvent event) {
    bool ok;

    ok = SendPointerEvent(clientRFB, x, y, event);
    SendPointerEvent(clientRFB, x, y, 0);


    return ok;
}

/**
 * @brief Sends a key event
 * @param key The key
 * @param down The key is down or not
 * @return If everything is ok or not
 * @details Sends a key event to the server
 */
bool ClientConnectionRFB::sendKeyEvent(int key, bool down) {
    rfbKeySym rfbKey = transformToRfbKey(key);
    if (rfbKey != 0) {
        SendKeyEvent(clientRFB, rfbKey, down);
    }


}

/**
 * @brief Changes the key code to a rfbKeySym
 * @param key The key
 * @return The rfbKeySym
 * @details Changes the current key code to a rfbKeySym
 */
rfbKeySym ClientConnectionRFB::transformToRfbKey(int key) {
    rfbKeySym rfbKey = 0;

    switch (key) {
        case 66:
            rfbKey = XK_KP_Enter;
            break;
        case 62:
            rfbKey = XK_space;
            break;
        case 67:
            rfbKey = XK_BackSpace;
            break;
        case 1:
            rfbKey = XK_Control_L;
            break;//control
        case 2:
            rfbKey = XK_Shift_L;
            break;//shift
        case 3:
            rfbKey = XK_Alt_L;
            break;//alt
        case 4:
            rfbKey = XK_Delete;
            break;//supr
        case 5:
            rfbKey = XK_Meta_L;
            break;//meta

        case 17:
            rfbKey = XK_F1;
            break;
        case 18:
            rfbKey = XK_F2;
            break;
        case 19:
            rfbKey = XK_F3;
            break;
        case 20:
            rfbKey = XK_F4;
            break;
        case 21:
            rfbKey = XK_F5;
            break;
        case 22:
            rfbKey = XK_F6;
            break;
        case 23:
            rfbKey = XK_F7;
            break;
        case 24:
            rfbKey = XK_F8;
            break;
        case 25:
            rfbKey = XK_F9;
            break;
        case 26:
            rfbKey = XK_F10;
            break;
        case 27:
            rfbKey = XK_F11;
            break;
        case 28:
            rfbKey = XK_F12;
            break;


        case 29:
            rfbKey = XK_a;
            break;
        case 30:
            rfbKey = XK_b;
            break;
        case 31:
            rfbKey = XK_c;
            break;
        case 32:
            rfbKey = XK_d;
            break;
        case 33:
            rfbKey = XK_e;
            break;
        case 34:
            rfbKey = XK_f;
            break;
        case 35:
            rfbKey = XK_g;
            break;
        case 36:
            rfbKey = XK_h;
            break;
        case 37:
            rfbKey = XK_i;
            break;
        case 38:
            rfbKey = XK_j;
            break;
        case 39:
            rfbKey = XK_k;
            break;
        case 40:
            rfbKey = XK_l;
            break;
        case 41:
            rfbKey = XK_m;
            break;
        case 42:
            rfbKey = XK_n;
            break;
        case 43:
            rfbKey = XK_o;
            break;
        case 44:
            rfbKey = XK_p;
            break;
        case 45:
            rfbKey = XK_q;
            break;
        case 46:
            rfbKey = XK_r;
            break;
        case 47:
            rfbKey = XK_s;
            break;
        case 48:
            rfbKey = XK_t;
            break;
        case 49:
            rfbKey = XK_u;
            break;
        case 50:
            rfbKey = XK_v;
            break;
        case 51:
            rfbKey = XK_w;
            break;
        case 52:
            rfbKey = XK_x;
            break;
        case 53:
            rfbKey = XK_y;
            break;
        case 54:
            rfbKey = XK_z;
            break;

        case 129:
            rfbKey = XK_A;
            break;
        case 130:
            rfbKey = XK_B;
            break;
        case 131:
            rfbKey = XK_C;
            break;
        case 132:
            rfbKey = XK_D;
            break;
        case 133:
            rfbKey = XK_E;
            break;
        case 134:
            rfbKey = XK_F;
            break;
        case 135:
            rfbKey = XK_G;
            break;
        case 136:
            rfbKey = XK_H;
            break;
        case 137:
            rfbKey = XK_I;
            break;
        case 138:
            rfbKey = XK_J;
            break;
        case 139:
            rfbKey = XK_K;
            break;
        case 140:
            rfbKey = XK_L;
            break;
        case 141:
            rfbKey = XK_M;
            break;
        case 142:
            rfbKey = XK_N;
            break;
        case 143:
            rfbKey = XK_O;
            break;
        case 144:
            rfbKey = XK_P;
            break;
        case 145:
            rfbKey = XK_Q;
            break;
        case 146:
            rfbKey = XK_R;
            break;
        case 147:
            rfbKey = XK_S;
            break;
        case 148:
            rfbKey = XK_T;
            break;
        case 149:
            rfbKey = XK_U;
            break;
        case 150:
            rfbKey = XK_V;
            break;
        case 151:
            rfbKey = XK_W;
            break;
        case 152:
            rfbKey = XK_X;
            break;
        case 153:
            rfbKey = XK_Y;
            break;
        case 154:
            rfbKey = XK_Z;
            break;

        case 7:
            rfbKey = XK_0;
            break;
        case 8:
            rfbKey = XK_1;
            break;
        case 9:
            rfbKey = XK_2;
            break;
        case 10:
            rfbKey = XK_3;
            break;
        case 11:
            rfbKey = XK_4;
            break;
        case 12:
            rfbKey = XK_5;
            break;
        case 13:
            rfbKey = XK_6;
            break;
        case 14:
            rfbKey = XK_7;
            break;
        case 15:
            rfbKey = XK_8;
            break;
        case 16:
            rfbKey = XK_9;
            break;

            // Acento XK_acute
            //simbolos
        case 108:
            rfbKey = XK_exclam;
            break;//exclamacion
        case 109:
            rfbKey = XK_at;
            break;//arroba
        case 110:
            rfbKey = XK_numbersign;
            break;//#
        case 111:
            rfbKey = XK_dollar;
            break;//dolar
        case 112:
            rfbKey = XK_percent;
            break;//porcentaje
        case 113:
            rfbKey = XK_dead_circumflex;
            break;//^
        case 114:
            rfbKey = XK_ampersand;
            break;//&
        case 115:
            rfbKey = XK_asterisk;
            break;//asterisco
        case 116:
            rfbKey = XK_parenleft;
            break;//(
        case 107:
            rfbKey = XK_parenright;
            break;//)
        case 76:
            rfbKey = XK_slash;
            break;// /
        case 174:
            rfbKey = XK_colon;
            break;//:
        case 74:
            rfbKey = XK_semicolon;
            break;// ;
        case 70:
            rfbKey = XK_equal;
            break;// =
        case 170:
            rfbKey = XK_plus;
            break; // +
        case 176:
            rfbKey = XK_question;
            break;//?
        case 56:
            rfbKey = XK_KP_Decimal;
            break;// .
        case 55:
            rfbKey = XK_comma;
            break;//,
        case 68:
            rfbKey = XK_grave;
            break;//` //PROBAR
        case 69:
            rfbKey = XK_minus;
            break;//-
        case 169:
            rfbKey = XK_underscore;
            break;//_
        case 171:
            rfbKey = XK_braceleft;
            break;//{
        case 172:
            rfbKey = XK_braceright;
            break;//}
        case 173:
            rfbKey = XK_bar;
            break;//|
        case 71:
            rfbKey = XK_bracketleft;
            break;//[
        case 72:
            rfbKey = XK_bracketright;
            break;//]
        case 155:
            rfbKey = XK_less;
            break;//<
        case 156:
            rfbKey = XK_greater;
            break;//>
        case 175:
            rfbKey = XK_quotedbl;
            break;//"
        case 73:
            rfbKey = XK_backslash;
            break;// contra barra
        case 168:
            rfbKey = XK_dead_tilde;
            break;//,

        default:
            break;
    }
    return rfbKey;
}
