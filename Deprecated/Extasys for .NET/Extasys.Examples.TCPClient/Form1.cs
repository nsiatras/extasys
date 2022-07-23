using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;

namespace Extasys.Examples.TCPClient
{
    public partial class Form1 : Form
    {
        private TCPClient fClient = new TCPClient("", "");
        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            fClient.Connectors.Clear();
            fClient.AddConnector("a", IPAddress.Parse(textBoxRemoteHostIP.Text), int.Parse(textBoxRemoteHostPort.Text), 65535,((char)2));
            fClient.Start();

            button2.Enabled = true;
            button1.Enabled = false;

            button3.Enabled = true;
            button4.Enabled = false;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            fClient.Stop();

            button1.Enabled = true;
            button2.Enabled = false;

            button3.Enabled = false;
            button4.Enabled = false;
        }

        private void button3_Click(object sender, EventArgs e)
        {
            fClient.StartSendingMessages();
            button3.Enabled = false;
            button4.Enabled = true;
        }

        private void button4_Click(object sender, EventArgs e)
        {
            fClient.StopSendingMessages();
            button3.Enabled = true;
            button4.Enabled = false;
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            fClient.StopSendingMessages();
            fClient.Stop();
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            labelBytesIn.Text = fClient.BytesIn.ToString();
            labelBytesOut.Text = fClient.BytesOut.ToString();
        }


    }
}
