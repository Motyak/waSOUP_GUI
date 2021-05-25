using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace waSOUP_GUI
{
    public partial class Form1 : Form
    {
        private Form2 form2;

        private BackendApi backend;
        private List<generatedIce.Track> tracks;
        private bool pause = true;

        private void updateList()
        {
            this.listTracks.Items.Clear();
            foreach (generatedIce.Track t in this.tracks)
                this.listTracks.Items.Add(new ListViewItem(t.title));
        }

        public Form1()
        {
            InitializeComponent();

            this.form2 = new Form2();

            this.backend = new BackendApi();

            // récupérer toutes les tracks et les ajouter à la liste
            this.tracks = this.backend.getAllTracks();
            this.updateList();
        }

        private void listTracks_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            Console.WriteLine(this.listTracks.SelectedItems[0].Text);
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            Console.WriteLine("ADD");

            this.form2.updateFields("", "");
            this.form2.ShowDialog();
            if (this.form2.Confirmed)
            {
                generatedIce.Track newTrack = new generatedIce.Track(
                        -1, this.form2.Title, this.form2.Artist, -1, "");
                newTrack = this.backend.update(newTrack, this.form2.Data);
                this.tracks.Add(newTrack);
                this.updateList();
            }
        }

        private void btnRemove_Click(object sender, EventArgs e)
        {
            if(this.listTracks.SelectedItems.Count == 0)
                Console.WriteLine("REMOVE ");
            else
                Console.WriteLine("REMOVE " + this.listTracks.SelectedItems[0].Text);

            var selectedRows = this.listTracks.SelectedIndices;
            if (selectedRows.Count > 0)
            {
                var selectedTrack = this.tracks.ElementAt(selectedRows[0]);
                this.backend.remove(selectedTrack);
                this.tracks.Remove(selectedTrack);
                this.updateList();
            }
        }

        private void btnEdit_Click(object sender, EventArgs e)
        {
            var selectedRows = this.listTracks.SelectedIndices;
            if (selectedRows.Count > 0)
            {
                var selectedTrack = this.tracks.ElementAt(selectedRows[0]);
                this.form2.updateFields(selectedTrack.title, selectedTrack.artist);
                this.form2.ShowDialog();

                if(this.form2.Confirmed)
                {
                    selectedTrack.title = this.form2.Title;
                    selectedTrack.artist = this.form2.Artist;
                    this.updateList();
                    this.backend.update(selectedTrack, this.form2.Data);
                }
            }


            if (this.listTracks.SelectedItems.Count == 0)
                Console.WriteLine("EDIT ");
            else
                Console.WriteLine("EDIT " + this.listTracks.SelectedItems[0].Text);
        }

        private void btnPlayPause_Click(object sender, EventArgs e)
        {
            this.pause = !this.pause;
            if (this.pause)
                Console.WriteLine("PLAY");
            else
                Console.WriteLine("PAUSE");
        }

        private void txtSearch_TextChanged(object sender, EventArgs e)
        {
            Console.WriteLine("text changed");
        }
    }
}
