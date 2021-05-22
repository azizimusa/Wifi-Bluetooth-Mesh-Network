package underdark.mesh.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;

public class MainActivity extends AppCompatActivity implements TransportListener {
    private boolean running;
    private long nodeId;
    private long selfNodeID;
    private Transport transport;
    private ArrayList<Link> links = new ArrayList<>();
    EditText input, output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.input);
        output = findViewById(R.id.receiver);

        initMesh();
    }

    private void initMesh() {

        do {
            long random = new Random().nextLong();
            nodeId = random;
            selfNodeID = nodeId;

        } while (nodeId == 0);

        if (nodeId < 0)
            nodeId = -nodeId;

        EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.BLUETOOTH);

        this.transport = Underdark.configureTransport(
                234235,
                nodeId,
                this,
                null,
                this,
                kinds
        );

        start();
    }

    public void start() {
        if (running)
            return;

        running = true;
        transport.start();
    }

    public void stop() {
        if (!running)
            return;

        running = false;
        transport.stop();
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void broadcastFrame(byte[] frameData) {

        if (links.isEmpty())
            return;

        for (Link link : links) {
            link.sendFrame(frameData);
        }
    }

    @Override
    public void transportNeedsActivity(Transport transport, ActivityCallback callback) {
        callback.accept((AppCompatActivity) this);
    }

    @Override
    public void transportLinkConnected(Transport transport, Link link) {
        links.add(link);
        Log.i("MESH", "adding " + link.getNodeId());
    }

    @Override
    public void transportLinkDisconnected(Transport transport, Link link) {
        links.remove(link);
        Log.i("MESH", "remove " + link.getNodeId());

    }

    @Override
    public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData) {
        String text = new String(frameData, StandardCharsets.UTF_8);
        Log.i("MESH", "receive " + text);

        if (link.getNodeId() != selfNodeID) {
            output.setText(text);
        }
    }

    public void sendMessage(View view) {
        String data = input.getText().toString();
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        broadcastFrame(bytes);
    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }
}