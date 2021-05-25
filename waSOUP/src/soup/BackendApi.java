package soup;

public class BackendApi
{
    private com.zeroc.Ice.Communicator iceCommunicator;
    private generatedIce.CollectionPrx collectionA;
    private java.util.HashMap<Track,generatedIce.CollectionPrx> locations;

    public static void main(String[] args)
    {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        BackendApi backend = null;
        try
        {
            backend = new BackendApi();
            Track track = new Track();
            java.nio.file.Path raymanFile = java.nio.file.Paths.get("../data/rayman.mp3");
            java.nio.file.Path timecrisisFile = java.nio.file.Paths.get("../data/timecrisis.mp3");
            byte[] raymanData = java.nio.file.Files.readAllBytes(raymanFile);
            byte[] timecrisisData = java.nio.file.Files.readAllBytes(timecrisisFile);

            System.out.println("Press enter to upload data 'rayman'");
            scanner.nextLine();
            track = backend.update(track, raymanData);

            System.out.println("Press enter to upload different data -> 'timecrisis'");
            scanner.nextLine();
            track = backend.update(track, timecrisisData);
            
            System.out.println("Press enter to rename timecrisis track to 'Time Crisis'");
            scanner.nextLine();
            track.title = "Time Crisis";
            backend.update(track, null);
            
            java.util.ArrayList<Track> tracks = backend.getAllTracks();
            for(Track m : tracks)
            	System.out.println(m.title);

            backend.remove(track);
        }
        catch(java.io.IOException | java.lang.OutOfMemoryError | java.lang.SecurityException e){e.printStackTrace();}
        finally
        {
            if(backend != null)
                backend.destroyIceCommunicator();
        }
    }

    public BackendApi()
    {
        this.locations = new java.util.HashMap<Track,generatedIce.CollectionPrx>();

        // Initialisation du communicateur Ice
        this.iceCommunicator = com.zeroc.Ice.Util.initialize(new String[]{});

        /* Recuperation du proxy pour la collection A */
        com.zeroc.Ice.ObjectPrx base = this.iceCommunicator.stringToProxy("Collection:default -p 10000");
        this.collectionA = generatedIce.CollectionPrx.checkedCast(base);
        if(this.collectionA == null)
            throw new Error("collectionA: Invalid proxy");
    }

    public void destroyIceCommunicator()
    {
        this.iceCommunicator.destroy();
    }

    public java.util.ArrayList<Track> getAllTracks()
    {
        // gather tracks across all servers

        java.util.ArrayList<Track> res = new java.util.ArrayList<>();

        /* collection A */
        generatedIce.Track[] collecA = this.collectionA.getAllTracks();
        for(generatedIce.Track t : collecA)
            res.add(new Track(t.id, t.title, t.artist, t.duration, t.md5));

        /* collection B */
        // les ajouter à l'array list

        return res;
    }

    public Track update(Track track, byte[] data)
    {
        // convert Track to generatedIce.Track
        generatedIce.Track t = new generatedIce.Track(track._id, track.title, track.artist, track._duration, track._md5);

        if(data == null || data.length == 0)
        {
            this.collectionA.updateIfExisting(t);
            return track;
        }
        else //if there is something to upload
        {
            // upload every 64KB chunk
            int nbOfChunks = data.length / 65536 + 1;
            int indexStartChunk = 0;
            for(int i = 1 ; i < nbOfChunks ; ++i)
            {
                byte[] chunk = java.util.Arrays.copyOfRange(data, indexStartChunk, indexStartChunk + 65536);
                this.collectionA.upload(t, chunk);
                indexStartChunk += 65536;
            }
            byte[] chunk = java.util.Arrays.copyOfRange(data, indexStartChunk, data.length);
            t = this.collectionA.finishUpload(t, chunk);
            return new Track(t.id, t.title, t.artist, t.duration, t.md5);
        }
    }

    public void remove(Track track) //lever une exception en cas d'err
    {
        // check track location and update to the related server

    	// convert Track to generatedIce.Track
        generatedIce.Track t = new generatedIce.Track(track._id, track.title, track.artist, track._duration, track._md5);

        this.collectionA.remove(t);
    }
}