namespace Extasys.Examples.TCPServer
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
            this.textBoxIP = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.textBoxMaxConnections = new System.Windows.Forms.TextBox();
            this.label5 = new System.Windows.Forms.Label();
            this.textBoxCOnnectionTimeOut = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.label6 = new System.Windows.Forms.Label();
            this.labelBytesIn = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.labelBytesOut = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.labelClientsConnected = new System.Windows.Forms.Label();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(62, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "TCP Server";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(34, 39);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(101, 13);
            this.label2.TabIndex = 1;
            this.label2.Text = "Listener IP Address:";
            // 
            // textBoxIP
            // 
            this.textBoxIP.Location = new System.Drawing.Point(170, 39);
            this.textBoxIP.Name = "textBoxIP";
            this.textBoxIP.Size = new System.Drawing.Size(211, 20);
            this.textBoxIP.TabIndex = 2;
            this.textBoxIP.Text = "127.0.0.1";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(34, 68);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(69, 13);
            this.label3.TabIndex = 3;
            this.label3.Text = "Listener Port:";
            // 
            // textBoxPort
            // 
            this.textBoxPort.Location = new System.Drawing.Point(170, 65);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(211, 20);
            this.textBoxPort.TabIndex = 4;
            this.textBoxPort.Text = "5000";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(34, 92);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(92, 13);
            this.label4.TabIndex = 5;
            this.label4.Text = "Max Connections:";
            // 
            // textBoxMaxConnections
            // 
            this.textBoxMaxConnections.Location = new System.Drawing.Point(170, 92);
            this.textBoxMaxConnections.Name = "textBoxMaxConnections";
            this.textBoxMaxConnections.Size = new System.Drawing.Size(211, 20);
            this.textBoxMaxConnections.TabIndex = 6;
            this.textBoxMaxConnections.Text = "1000";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(34, 121);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(132, 13);
            this.label5.TabIndex = 7;
            this.label5.Text = "Connection Time-Out (ms):";
            // 
            // textBoxCOnnectionTimeOut
            // 
            this.textBoxCOnnectionTimeOut.Location = new System.Drawing.Point(170, 118);
            this.textBoxCOnnectionTimeOut.Name = "textBoxCOnnectionTimeOut";
            this.textBoxCOnnectionTimeOut.Size = new System.Drawing.Size(211, 20);
            this.textBoxCOnnectionTimeOut.TabIndex = 8;
            this.textBoxCOnnectionTimeOut.Text = "10000";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(37, 159);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 9;
            this.button1.Text = "Start Server";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.Enabled = false;
            this.button2.Location = new System.Drawing.Point(118, 159);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(75, 23);
            this.button2.TabIndex = 10;
            this.button2.Text = "Stop Server";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(34, 206);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(48, 13);
            this.label6.TabIndex = 11;
            this.label6.Text = "Bytes In:";
            // 
            // labelBytesIn
            // 
            this.labelBytesIn.AutoSize = true;
            this.labelBytesIn.Location = new System.Drawing.Point(138, 206);
            this.labelBytesIn.Name = "labelBytesIn";
            this.labelBytesIn.Size = new System.Drawing.Size(13, 13);
            this.labelBytesIn.TabIndex = 12;
            this.labelBytesIn.Text = "0";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(34, 231);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(56, 13);
            this.label7.TabIndex = 13;
            this.label7.Text = "Bytes Out:";
            // 
            // labelBytesOut
            // 
            this.labelBytesOut.AutoSize = true;
            this.labelBytesOut.Location = new System.Drawing.Point(138, 231);
            this.labelBytesOut.Name = "labelBytesOut";
            this.labelBytesOut.Size = new System.Drawing.Size(13, 13);
            this.labelBytesOut.TabIndex = 14;
            this.labelBytesOut.Text = "0";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(34, 253);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(96, 13);
            this.label8.TabIndex = 15;
            this.label8.Text = "Clients Connected:";
            // 
            // labelClientsConnected
            // 
            this.labelClientsConnected.AutoSize = true;
            this.labelClientsConnected.Location = new System.Drawing.Point(138, 253);
            this.labelClientsConnected.Name = "labelClientsConnected";
            this.labelClientsConnected.Size = new System.Drawing.Size(13, 13);
            this.labelClientsConnected.TabIndex = 16;
            this.labelClientsConnected.Text = "0";
            // 
            // timer1
            // 
            this.timer1.Enabled = true;
            this.timer1.Interval = 2000;
            this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(419, 307);
            this.Controls.Add(this.labelClientsConnected);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.labelBytesOut);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.labelBytesIn);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.textBoxCOnnectionTimeOut);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.textBoxMaxConnections);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.textBoxPort);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.textBoxIP);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Name = "Form1";
            this.Text = "Extasys Example TCP Server";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox textBoxIP;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox textBoxMaxConnections;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox textBoxCOnnectionTimeOut;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label labelBytesIn;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label labelBytesOut;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label labelClientsConnected;
        private System.Windows.Forms.Timer timer1;
    }
}

