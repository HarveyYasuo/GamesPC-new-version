/**
 * Cloud Functions para GamesPC.
 *
 * sendChatMessageNotification: cuando un usuario anónimo escribe un mensaje en el
 * chat general (nodo "chat" de Realtime Database), se envía una notificación FCM
 * al tema "general_chat" (al que todas las instalaciones de la app se suscriben).
 *
 * Para desplegar:
 *   cd functions
 *   npm install
 *   npx firebase-tools login
 *   npx firebase-tools deploy --only functions
 */

const { setGlobalOptions } = require("firebase-functions");
const { onValueCreated } = require("firebase-functions/v2/database");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

// Tema FCM al que la app se suscribe en MyApplication.kt y onNewToken.
// Debe coincidir exactamente con el usado en la app.
const CHAT_TOPIC = "general_chat";

exports.sendChatMessageNotification = onValueCreated(
  {
    ref: "chat/{messageId}",
    // Si tu Realtime Database está en otra región (p. ej. europe-west1),
    // añade: region: "europe-west1"
  },
  async (event) => {
    const message = event.data.val();
    if (!message || typeof message !== "object") return;

    const text = String(message.text || "").trim();
    if (!text) return;

    const senderName = String(message.senderName || "").trim() || "Anónimo";
    const senderId = String(message.userId || "");

    const payload = {
      notification: {
        title: `Nuevo mensaje de ${senderName}`,
        body: text.length > 200 ? `${text.substring(0, 200)}…` : text,
      },
      data: {
        type: "chat",
        messageId: event.params.messageId,
        senderId,
        senderName,
        text,
      },
      topic: CHAT_TOPIC,
    };

    try {
      await admin.messaging().send(payload);
      logger.info(`Notificación de chat enviada (${senderName})`);
    } catch (error) {
      logger.error("Error enviando notificación de chat", error);
    }
  }
);
