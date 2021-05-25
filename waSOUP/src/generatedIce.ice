module generatedIce
{
    struct Track
    {
        int id;
        string title;
        string artist;
        int duration;
        string md5;
    }

    sequence<Track> Tracks;
    sequence<byte> Bytes;

    interface Collection
    {
        Tracks getAllTracks();
        void upload(Track track, Bytes dataChunk);
        Track finishUpload(Track track, Bytes dataChunk);
        void updateIfExisting(Track track);
        void remove(Track track);
    }

    interface Streaming
    {
        void play(Track track);
        void togglePause();
        void skipTime(int delta);
        void stop();
    }
}
