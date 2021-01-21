
package me.kec.mp.bare;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.reactivestreams.FlowAdapters;

@Path("/greet")
@ApplicationScoped
public class GreetResource {

    private static final Logger LOGGER = Logger.getLogger(GreetResource.class.getName());

    private final SubmissionPublisher<CloudEvent> emitter = new SubmissionPublisher<>();

    private SseBroadcaster sseBroadcaster;

    private final CloudEventBuilder eventTemplate = CloudEventBuilder.v1()
            .withSource(URI.create("localhost:7001/greet"))
            .withType("helidon.messaging.kafka.oci.example");

    @Incoming("from-stream")
    public void receive(CloudEvent event) {
        System.out.println(event);
        if (sseBroadcaster == null) {
            LOGGER.warning("No SSE client subscribed yet: " + event);
            return;
        }
        sseBroadcaster.broadcast(new OutboundEvent.Builder().data(new String(event.getData().toBytes())).build());
    }

    @Outgoing("to-stream")
    public PublisherBuilder<CloudEvent> registerEmitter() {
        return ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(emitter));
    }

    @POST
    @Path("/send/{msg}")
    public void send(@PathParam("msg") String payload) {
        CloudEvent cloudEvent = eventTemplate
                .withId(UUID.randomUUID().toString())
                .withData(MediaType.TEXT_PLAIN, payload.getBytes())
                .build();
        emitter.submit(cloudEvent);
    }

    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToEvents(@Context SseEventSink eventSink, @Context Sse sse) {
        if (sseBroadcaster == null) {
            sseBroadcaster = sse.newBroadcaster();
        }
        sseBroadcaster.register(eventSink);
    }
}
