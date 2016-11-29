namespace Extasys.Examples.UDPClient
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.textBoxIP = new System.Windows.Forms.TextBox();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.textBoxReadTimeOut = new System.Windows.Forms.TextBox();
            this.buttonStartSendingMessages = new System.Windows.Forms.Button();
            this.buttonStopSendingMessages = new System.Windows.Forms.Button();
            this.buttonStartClient = new System.Windows.Forms.Button();
            this.buttonStopClient = new System.Windows.Forms.Button();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.labelBytesIn = new System.Windows.Forms.Label();
            this.labelBytesOut = new System.Windows.Forms.Label();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(59, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "UDP Client";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(32, 41);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(83, 13);
            this.label2.TabIndex = 1;
            this.label2.Text = "Remote host IP:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(32, 67);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(92, 13);
            this.label3.TabIndex = 2;
            this.label3.Text = "Remote host Port:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(32, 90);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(82, 13);
            this.label4.TabIndex = 3;
            this.label4.Text = "Read Time-Out:";
            // 
            // textBoxIP
            // 
            this.textBoxIP.Location = new System.Drawing.Point(145, 38);
            this.textBoxIP.Name = "textBoxIP";
            this.textBoxIP.Size = new System.Drawing.Size(192, 20);
            this.textBoxIP.TabIndex = 4;
            this.textBoxIP.Text = "127.0.0.1";
            // 
            // textBoxPort
            // 
            this.textBoxPort.Location = new System.Drawing.Point(145, 64);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(192, 20);
            this.textBoxPort.TabIndex = 5;
            this.textBoxPort.Text = "5000";
            // 
            // textBoxReadTimeOut
            // 
            this.textBoxReadTimeOut.Location = new System.Drawing.Point(145, 90);
            this.textBoxReadTimeOut.Name = "textBoxReadTimeOut";
            this.textBoxReadTimeOut.Size = new System.Drawing.Size(192, 20);
            this.textBoxReadTimeOut.TabIndex = 6;
            this.textBoxReadTimeOut.Text = "8000";
            // 
            // buttonStartSendingMessages
            // 
            this.buttonStartSendingMessages.Enabled = false;
            this.buttonStartSendingMessages.Location = new System.Drawing.Point(35, 177);
            this.buttonStartSendingMessages.Name = "buttonStartSendingMessages";
            this.buttonStartSendingMessages.Size = new System.Drawing.Size(144, 23);
            this.buttonStartSendingMessages.TabIndex = 7;
            this.buttonStartSendingMessages.Text = "Start Sending Messages";
            this.buttonStartSendingMessages.UseVisualStyleBackColor = true;
            this.buttonStartSendingMessages.Click += new System.EventHandler(this.buttonStartSendingMessages_Click);
            // 
            // buttonStopSendingMessages
            // 
            this.buttonStopSendingMessages.Enabled = false;
            this.buttonStopSendingMessages.Location = new System.Drawing.Point(193, 177);
            this.buttonStopSendingMessages.Name = "buttonStopSendingMessages";
            this.buttonStopSendingMessages.Size = new System.Drawing.Size(144, 23);
            this.buttonStopSendingMessages.TabIndex = 8;
            this.buttonStopSendingMessages.Text = "Stop Sending Messages";
            this.buttonStopSendingMessages.UseVisualStyleBackColor = true;
            this.buttonStopSendingMessages.Click += new System.EventHandler(this.buttonStopSendingMessages_Click);
            // 
            // buttonStartClient
            // 
            this.buttonStartClient.Location = new System.Drawing.Point(35, 125);
            this.buttonStartClient.Name = "buttonStartClient";
            this.buttonStartClient.Size = new System.Drawing.Size(75, 23);
            this.buttonStartClient.TabIndex = 9;
            this.buttonStartClient.Text = "Start Client";
            this.buttonStartClient.UseVisualStyleBackColor = true;
            this.buttonStartClient.Click += new System.EventHandler(this.buttonStartClient_Click);
            // 
            // buttonStopClient
            // 
            this.buttonStopClient.Enabled = false;
            this.buttonStopClient.Location = new System.Drawing.Point(116, 125);
            this.buttonStopClient.Name = "buttonStopClient";
            this.buttonStopClient.Size = new System.Drawing.Size(75, 23);
            this.buttonStopClient.TabIndex = 10;
            this.buttonStopClient.Text = "Stop Client";
            this.buttonStopClient.UseVisualStyleBackColor = true;
            this.buttonStopClient.Click += new System.EventHandler(this.buttonStopClient_Click);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(35, 224);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(48, 13);
            this.label5.TabIndex = 11;
            this.label5.Text = "Bytes In:";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(35, 247);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(56, 13);
            this.label6.TabIndex = 12;
            this.label6.Text = "Bytes Out:";
            // 
            // labelBytesIn
            // 
            this.labelBytesIn.AutoSize = true;
            this.labelBytesIn.Location = new System.Drawing.Point(101, 224);
            this.labelBytesIn.Name = "labelBytesIn";
            this.labelBytesIn.Size = new System.Drawing.Size(13, 13);
            this.labelBytesIn.TabIndex = 13;
            this.labelBytesIn.Text = "0";
            // 
            // labelBytesOut
            // 
            this.labelBytesOut.AutoSize = true;
            this.labelBytesOut.Location = new System.Drawing.Point(101, 247);
            this.labelBytesOut.Name = "labelBytesOut";
            this.labelBytesOut.Size = new System.Drawing.Size(13, 13);
            this.labelBytesOut.TabIndex = 14;
            this.labelBytesOut.Text = "0";
            // 
            // timer1
            // 
            this.timer1.Enabled = true;
            this.timer1.Interval = 1000;
            this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(407, 278);
            this.Controls.Add(this.labelBytesOut);
            this.Controls.Add(this.labelBytesIn);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.buttonStopClient);
            this.Controls.Add(this.buttonStartClient);
            this.Controls.Add(this.buttonStopSendingMessages);
            this.Controls.Add(this.buttonStartSendingMessages);
            this.Controls.Add(this.textBoxReadTimeOut);
            this.Controls.Add(this.textBoxPort);
            this.Controls.Add(this.textBoxIP);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Name = "Form1";
            this.Text = "Extasys Examples UDP Client";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox textBoxIP;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.TextBox textBoxReadTimeOut;
        private System.Windows.Forms.Button buttonStartSendingMessages;
        private System.Windows.Forms.Button buttonStopSendingMessages;
        private System.Windows.Forms.Button buttonStartClient;
        private System.Windows.Forms.Button buttonStopClient;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label labelBytesIn;
        private System.Windows.Forms.Label labelBytesOut;
        private System.Windows.Forms.Timer timer1;
    }
}

