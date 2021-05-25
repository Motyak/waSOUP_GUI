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
    public partial class Form2 : Form
    {
        private bool _confirmed = false;
        private String _title = "";
        private String _artist = "";

        public bool Confirmed
        {
            get { return this._confirmed; }
        }

        public String Title
        {
            get { return this._title; }
            set { this._title = value; }
        }

        public String Artist
        {
            get { return this._artist; }
            set { this._artist = value; }
        }

        public void updateFields(String title, String artist)
        {
            this.txtTitle.Text = title;
            this.txtArtist.Text = artist;
        }

        public Form2()
        {
            InitializeComponent();

            this.txtTitle.Text = this._title;
            this.txtArtist.Text = this._artist;
        }

        private void btnOk_Click(object sender, EventArgs e)
        {
            this._title = this.txtTitle.Text;
            this._artist = this.txtArtist.Text;
            this._confirmed = true;
            this.Close();
        }

        private void Form2_Load(object sender, EventArgs e)
        {
            // reset 'confirmed' flag
            this._confirmed = false;
        }
    }
}
