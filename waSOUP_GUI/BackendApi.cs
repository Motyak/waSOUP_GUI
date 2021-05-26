using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace waSOUP_GUI
{
    class BackendApi
    {
        private Ice.Communicator iceCommunicator;
        private generatedIce.CollectionPrx collection;
        private generatedIce.StreamingPrx streaming;

        public BackendApi()
        {
            // Initialisation du communicateur Ice
            this.iceCommunicator = Ice.Util.initialize();

            /* Recuperation du proxy pour la collection */
            this.collection = generatedIce.CollectionPrxHelper.checkedCast(this.iceCommunicator.stringToProxy("Collection:default -p 10000"));
            if (this.collection == null)
                throw new Exception("collection: Invalid proxy");

            /* Recuperation du proxy pour le streaming */
            this.streaming = generatedIce.StreamingPrxHelper.checkedCast(this.iceCommunicator.stringToProxy("Streaming:default -p 10001"));
            if (this.streaming == null)
                throw new Exception("streaming: Invalid proxy");
        }

        public void destroyIceCommunicator()
        {
            this.iceCommunicator.destroy();
        }

        public List<generatedIce.Track> getAllTracks()
        {
            return this.collection.getAllTracks().ToList();
        }

        public generatedIce.Track update(generatedIce.Track track, byte[] data)
        {
            // convert Track to generatedIce.Track
            generatedIce.Track t = new generatedIce.Track(track.id, track.title, track.artist, track.duration, track.md5);

            if (data == null || data.Length == 0)
            {
                this.collection.updateIfExisting(t);
                return track;
            }
            else //if there is something to upload
            {
                // upload every 64KB chunk
                int nbOfChunks = data.Length / 65536 + 1;
                int indexStartChunk = 0;
                byte[] chunk = new byte[65536];
                for (int i = 1; i < nbOfChunks; ++i)
                {
                    Array.Copy(data, indexStartChunk, chunk, 0, 65536);
                    this.collection.upload(t, chunk);
                    indexStartChunk += 65536;
                }
                Array.Copy(data, indexStartChunk, chunk, 0, data.Length % 65536);
                return this.collection.finishUpload(t, chunk);
            }
        }

        public void remove(generatedIce.Track track)
        {
            this.collection.remove(track);
        }

        public void play(generatedIce.Track track)
        {
            this.streaming.play(track);
        }

        public void togglePause()
        {
            this.streaming.togglePause();
        }

        public void skipTime(int delta)
        {
            this.streaming.skipTime(delta);
        }

        public void stop()
        {
            this.streaming.stop();
        }
    }
}
