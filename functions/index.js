const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNewEventNotification = functions.database
  .ref('/Events/{eventId}')
  .onCreate((snapshot, context) => {
    const event = snapshot.val();

    const payload = {
      notification: {
        title: '🚴‍♂️ New Cycling Event!',
        body: `${event.title} has been posted.`,
        click_action: 'FLUTTER_NOTIFICATION_CLICK', // or your app package intent
      }
    };

    return admin.messaging().sendToTopic('allUsers', payload);
  });
