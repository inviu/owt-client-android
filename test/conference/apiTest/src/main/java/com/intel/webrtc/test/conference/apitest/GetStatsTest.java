package com.intel.webrtc.test.conference.apitest;

import static com.intel.webrtc.test.conference.util.ConferenceAction.createClient;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getRemoteForwardStream;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getStats;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getToken;
import static com.intel.webrtc.test.conference.util.ConferenceAction.join;
import static com.intel.webrtc.test.conference.util.ConferenceAction.leave;
import static com.intel.webrtc.test.conference.util.ConferenceAction.publish;
import static com.intel.webrtc.test.conference.util.ConferenceAction.stop;
import static com.intel.webrtc.test.conference.util.ConferenceAction.subscribe;
import static com.intel.webrtc.test.util.CommonAction.createDefaultCapturer;
import static com.intel.webrtc.test.util.CommonAction.createLocalStream;
import static com.intel.webrtc.test.util.Config.MIXED_STREAM_SIZE;
import static com.intel.webrtc.test.util.Config.PRESENTER_ROLE;
import static com.intel.webrtc.test.util.Config.USER1_NAME;
import static com.intel.webrtc.test.util.Config.USER2_NAME;

import com.intel.webrtc.conference.Publication;
import com.intel.webrtc.conference.RemoteStream;
import com.intel.webrtc.conference.Subscription;
import com.intel.webrtc.test.conference.util.ConferenceClientObserver;

public class GetStatsTest extends TestBase {

    public void testGetStats_publicationStatsAfterPublicationStop_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        Publication publication = publish(client1, localStream1, null, observer1, true);
        stop(publication, observer1, true);
        getStats(publication, false);
    }

    public void testGetStats_subscriptionStatsAfterSubscriptionStop_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, observer1, true);
        int streamsN = client1.info().getRemoteStreams().size() - MIXED_STREAM_SIZE;
        RemoteStream forwardStream = getRemoteForwardStream(client1, streamsN - 1);
        Subscription subscription = subscribe(client1, forwardStream, null, true, true);
        stop(subscription, forwardStream, true);
        getStats(subscription, false);
    }

    public void testGetStats_publicationStatsAfterLeave_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        Publication publication = publish(client1, localStream1, null, observer1, true);
        leave(client1, observer1, null);
        getStats(publication, false);
        client1 = null;
    }

    public void testGetStats_subscriptionStatsAfterLeave_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, observer1, true);
        int streamsN = client1.info().getRemoteStreams().size() - MIXED_STREAM_SIZE;
        RemoteStream forwardStream = getRemoteForwardStream(client1, streamsN - 1);
        Subscription subscription = subscribe(client1, forwardStream, null, true, true);
        leave(client1, observer1, null);
        getStats(subscription, false);
        client1 = null;
    }

    public void testGetStats_subscriptionStatsAfterRemoteStreamEnded_shouldSuccess() {
        client1 = createClient(null);
        observer2 = new ConferenceClientObserver(USER2_NAME, 1);
        client2 = createClient(observer2);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        join(client2, getToken(PRESENTER_ROLE, USER2_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        Publication publication = publish(client1, localStream1, null, observer2, true);
        int streamsN = client2.info().getRemoteStreams().size() - MIXED_STREAM_SIZE;
        RemoteStream forwardStream = getRemoteForwardStream(client2, streamsN - 1);
        Subscription subscription = subscribe(client2, forwardStream, null, true, true);
        stop(publication, observer2, true);
        getStats(subscription, true);
    }
}