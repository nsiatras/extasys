using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;

namespace Extasys.Examples.TCPServer
{
    public partial class Form1 : Form
    {
        private TCPServer fServer = new TCPServer("Server", "");

        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            fServer.Listeners.Clear();
            fServer.AddListener("A", IPAddress.Parse(textBoxIP.Text), int.Parse(textBoxPort.Text), int.Parse(textBoxMaxConnections.Text), 65535, int.Parse(textBoxCOnnectionTimeOut.Text), 100,((char)2));
            fServer.Start();

            button1.Enabled = false;
            button2.Enabled = true;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            fServer.Stop();

            button1.Enabled = true;
            button2.Enabled = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            labelBytesIn.Text = fServer.BytesIn.ToString();
            labelBytesOut.Text = fServer.BytesOut.ToString();
            labelClientsConnected.Text = fServer.CurrentConnectionsNumber.ToString();
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            fServer.Stop();
        }

        private void Form1_Load(object sender, EventArgs e)
        {

        }
    }
}
