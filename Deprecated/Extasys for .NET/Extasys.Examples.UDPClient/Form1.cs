using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;
using System.Threading;

namespace Extasys.Examples.UDPClient
{
    public partial class Form1 : Form
    {
        private UDPClient fClient = new UDPClient("", "");
        private Thread fKeepSendingMessagesThread;

        public Form1()
        {
            InitializeComponent();
        }

        private void buttonStartClient_Click(object sender, EventArgs e)
        {
            fClient.RemoveConnector("a");
            fClient.AddConnector("a", 65535, int.Parse(textBoxReadTimeOut.Text), IPAddress.Parse(textBoxIP.Text), int.Parse(textBoxPort.Text));
            fClient.Start();

            buttonStartClient.Enabled = false;
            buttonStopClient.Enabled = true;
            buttonStartSendingMessages.Enabled = true;
            buttonStopSendingMessages.Enabled = false;
        }

        private void buttonStopClient_Click(object sender, EventArgs e)
        {
            fClient.Stop();

            buttonStartClient.Enabled = true;
            buttonStopClient.Enabled = false;
            buttonStartSendingMessages.Enabled = false;
            buttonStopSendingMessages.Enabled = false;

            if (fKeepSendingMessagesThread != null)
            {
                try
                {
                    fKeepSendingMessagesThread.Abort();
                }
                catch (Exception ex)
                {

                }
            }
        }

        private void buttonStartSendingMessages_Click(object sender, EventArgs e)
        {
            fKeepSendingMessagesThread = new Thread(new ThreadStart(KeepSendingMessages));
            fKeepSendingMessagesThread.Start();

            buttonStartSendingMessages.Enabled = false;
            buttonStopSendingMessages.Enabled = true;
        }

        private void KeepSendingMessages()
        {
            while (true)
            {
                byte[] bytes = Encoding.Default.GetBytes(DateTime.Now.Ticks.ToString());
                fClient.SendData(bytes,0,bytes.Length);
                Thread.Sleep(5);
            }
        }

        private void buttonStopSendingMessages_Click(object sender, EventArgs e)
        {
            if (fKeepSendingMessagesThread != null)
            {
                try
                {
                    fKeepSendingMessagesThread.Abort();
                }
                catch (Exception ex)
                {

                }
            }

            buttonStartSendingMessages.Enabled = true;
            buttonStopSendingMessages.Enabled = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            if (fClient != null)
            {
                labelBytesIn.Text = fClient.BytesIn.ToString();
                labelBytesOut.Text = fClient.BytesOut.ToString();
            }
        }

        
    }
}
