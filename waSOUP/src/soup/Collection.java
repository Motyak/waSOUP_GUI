package soup;

public class Collection implements generatedIce.Collection
{
    final private String COLLECTION_DB_PATH = "../collection/tracks.db";
    final private String COLLECTION_PATH = "../collection/tracks/";
    
    private java.sql.Connection sql;
    private java.util.HashMap<generatedIce.Track,byte[]> uploading;

    public Collection() throws java.sql.SQLException
    {
        this.sql = java.sql.DriverManager.getConnection("jdbc:sqlite:" + this.COLLECTION_DB_PATH);
        this.uploading = new java.util.HashMap<generatedIce.Track,byte[]>();
    }

    public generatedIce.Track[] getAllTracks(com.zeroc.Ice.Current current)
    {
        System.out.println("getAllTracks");

        java.util.ArrayList<generatedIce.Track> res = new java.util.ArrayList<>();
        try
        {
            java.sql.Statement statement = this.sql.createStatement();
            statement.setQueryTimeout(10);
            java.sql.ResultSet rs = statement.executeQuery("select * from tracks");
            while(rs.next())
                res.add(new generatedIce.Track(rs.getInt("id"), rs.getString("title"), rs.getString("artist"), rs.getInt("duration"), rs.getString("md5")));
        }
        catch(java.sql.SQLException e){e.printStackTrace();}

        
        generatedIce.Track[] array = new generatedIce.Track[res.size()];
        return res.toArray(array);
    }

    public void upload(generatedIce.Track track, byte[] dataChunk, com.zeroc.Ice.Current current)
    {
        System.out.println("upload");
        this.addChunk(track, dataChunk);
    }

    public generatedIce.Track finishUpload(generatedIce.Track track, byte[] dataChunk, com.zeroc.Ice.Current current)
    {
        System.out.println("finishUpload");

        this.addChunk(track, dataChunk);

        byte[] data = this.uploading.get(track);

        /* calculate data hash for the filename */
        String hash = null;
        try
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(data);
            hash = javax.xml.bind.DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
        }
        catch(java.security.NoSuchAlgorithmException e){e.printStackTrace();}

        /* add file to collection (filesystem) */
        java.io.File file = new java.io.File(this.COLLECTION_PATH + hash + ".mp3");
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file))
        {
            fos.write(data); //overwrite if existing file
        }
        catch(java.io.IOException e){e.printStackTrace();}

        /* get track duration from file */
        short duration = -1;
        try
        {
            javax.sound.sampled.AudioFileFormat fileFormat = new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().getAudioFileFormat(file);
            java.util.Map properties = fileFormat.properties();
            duration = (short)(((Long) properties.get("duration")) / 1000000);
        }
        catch(java.io.IOException | javax.sound.sampled.UnsupportedAudioFileException e){e.printStackTrace();}

        /* add track to database or update if existing, delete old file if update and no dependencies */
        try
        {
            java.sql.Statement statement = this.sql.createStatement();
            statement.setQueryTimeout(10);
            String reqSelect = "select id from tracks where id=" + track.id;
            String reqInsert = "insert into tracks(title,artist,duration,md5) values('" 
                    + track.title + "','" + track.artist + "','" + duration 
                    + "','" + hash + "')";
            String reqUpdate = "update tracks set title='" + track.title + "',artist='" 
                    + track.artist + ",duration=" + duration + "',md5='" + hash
                    + "' where id=" + track.id;
            String reqLastId = "select id from tracks order by id desc limit 1";
            String reqNbTracksForMd5 = "select id from tracks where md5='" + track.md5 + "' limit 1";

            boolean existing = statement.executeQuery(reqSelect).next();
            if(existing)
            {
                statement.executeUpdate(reqUpdate);

                /* delete old file based on track object hash attribute if no more files depends on it */
                boolean someTrackDependsOnFile = statement.executeQuery(reqNbTracksForMd5).next();
                if(!someTrackDependsOnFile)
                {
                    java.nio.file.Path path = java.nio.file.Paths.get(this.COLLECTION_PATH + track.md5 + ".mp3");
                    java.nio.file.Files.deleteIfExists(path);
                }
                
                // on return la track avec le nouveau hash et la durée
                track.md5 = hash;
                track.duration = duration;
                return track;
            }
            else
            {
                statement.executeUpdate(reqInsert);

                // on return la track avec l'id généré, le hash et la durée
                track.id = statement.executeQuery(reqLastId).getInt("id");
                track.md5 = hash;
                track.duration = duration;
                return track;
            }
        }
        catch(java.sql.SQLException | java.io.IOException e){e.printStackTrace();}

        this.uploading.remove(track);

        return null; //en cas d'erreur
    }

    private void addChunk(generatedIce.Track track, byte[] chunk)
    {
        if(!this.uploading.containsKey(track))
            this.uploading.put(track, chunk);
        else
        {
            final byte[] currentData = this.uploading.get(track);

            byte[] newData = new byte[currentData.length + chunk.length];
            System.arraycopy(currentData, 0, newData, 0, currentData.length);
            System.arraycopy(chunk, 0, newData, currentData.length, chunk.length);

            this.uploading.put(track, newData);
        }
    }

    public void updateIfExisting(generatedIce.Track track, com.zeroc.Ice.Current current)
    {
        System.out.println("updateIfExisting");
        try
        {
            java.sql.Statement statement = this.sql.createStatement();
            statement.setQueryTimeout(10);
            String reqSelect = "select id from tracks where id=" + track.id;
            String reqUpdate = "update tracks set title='" + track.title + "',artist='" 
                    + track.artist + ",duration=" + track.duration + "',md5='" + track.md5
                    + "' where id=" + track.id;

            boolean existing = statement.executeQuery(reqSelect).next();
            if(existing)
                statement.executeUpdate(reqUpdate);
        }
        catch(java.sql.SQLException e){e.printStackTrace();}
    }

    public void remove(generatedIce.Track track, com.zeroc.Ice.Current current)
    {
        System.out.println("remove");
        try
        {
            java.sql.Statement statement = this.sql.createStatement();
            statement.setQueryTimeout(10);
            String reqDelete = "delete from tracks where id=" + track.id;
            String reqNbTracksForMd5 = "select id from tracks where md5='" + track.md5 + "' limit 1";

            statement.executeUpdate(reqDelete);

            /* delete old file based on track object hash attribute if no more files depends on it */
            boolean someTrackDependsOnFile = statement.executeQuery(reqNbTracksForMd5).next();
            if(!someTrackDependsOnFile)
            {
                java.nio.file.Path path = java.nio.file.Paths.get(this.COLLECTION_PATH + track.md5 + ".mp3");
                java.nio.file.Files.deleteIfExists(path);
            }
        }
        catch(java.io.IOException | java.sql.SQLException e){e.printStackTrace();}
    }

    public static class Server
    {
        public static void main(String[] args) throws java.sql.SQLException
        {
            try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args))
            {
                com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CollectionAdapter", "default -p 10000");
                com.zeroc.Ice.Object object = new Collection();
                adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("Collection"));
                adapter.activate();
                System.out.println("Collection server started.");
                communicator.waitForShutdown();
            }
        }
    }
}