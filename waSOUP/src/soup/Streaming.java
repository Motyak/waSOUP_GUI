package soup;

public class Streaming implements generatedIce.Streaming
{
    final private String COLLECTION_PATH = "../collection/tracks/";
    private String options;

    private uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer;

    public Streaming(String host, int port, String endpoint)
    {
        uk.co.caprica.vlcj.factory.MediaPlayerFactory mediaPlayerFactory = new uk.co.caprica.vlcj.factory.MediaPlayerFactory();
        this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();
        this.options = Streaming.formatRtspStream(host, port, endpoint);

        // TEST
        this.mediaPlayer.media().play(
            "rayman.mp3",
            this.options,
            ":no-sout-rtp-sap",
            ":no-sout-standard-sap",
            ":sout-all",
            ":sout-keep"
        );
    }

    public void play(generatedIce.Track track, com.zeroc.Ice.Current current)
    {
        this.mediaPlayer.media().play(
            track.md5, //attention la racine part de bin/ !!
            this.options,
            ":no-sout-rtp-sap",
            ":no-sout-standard-sap",
            ":sout-all",
            ":sout-keep"
        );
    }

    public void togglePause(com.zeroc.Ice.Current current)
    {
        this.mediaPlayer.controls().pause();
    }

    public void skipTime(int delta, com.zeroc.Ice.Current current)
    {
        this.mediaPlayer.controls().skipTime(delta);
    }

    public void stop(com.zeroc.Ice.Current current)
    {
        this.mediaPlayer.controls().stop();
    }

    private static String formatRtspStream(String serverAddress, int serverPort, String id)
    {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#rtp{sdp=rtsp://@");
        sb.append(serverAddress);
        sb.append(':');
        sb.append(serverPort);
        sb.append('/');
        sb.append(id);
        sb.append("}");
        return sb.toString();
    }

    public static class Server
    {
        public static void main(String[] args) throws java.sql.SQLException
        {
            try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args))
            {
                com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("StreamingAdapter", "default -p 10001");
                com.zeroc.Ice.Object object = new Streaming("127.0.0.1", 5555, "waSOUP");
                adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("Streaming"));
                adapter.activate();
                System.out.println("Streaming server started.");
                communicator.waitForShutdown();
            }
        }
    }
}