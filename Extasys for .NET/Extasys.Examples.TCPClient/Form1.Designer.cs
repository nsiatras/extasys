namespace Extasys.Examples.TCPClient
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
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.button3 = new System.Windows.Forms.Button();
            this.button4 = new System.Windows.Forms.Button();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.labelBytesIn = new System.Windows.Forms.Label();
            this.labelBytesOut = new System.Windows.Forms.Label();
            this.textBoxRemoteHostIP = new System.Windows.Forms.TextBox();
            this.textBoxRemoteHostPort = new System.Windows.Forms.TextBox();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 9);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(57, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "TCP Client";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(30, 35);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(83, 13);
            this.label2.TabIndex = 2;
            this.label2.Text = "Remote host IP:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(30, 59);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(91, 13);
            this.label3.TabIndex = 3;
            this.label3.Text = "Remote host port:";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(33, 99);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 4;
            this.button1.Text = "Start Client";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.Enabled = false;
            this.button2.Location = new System.Drawing.Point(114, 99);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(75, 23);
            this.button2.TabIndex = 5;
            this.button2.Text = "Stop Client";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // button3
            // 
            this.button3.Enabled = false;
            this.button3.Location = new System.Drawing.Point(195, 99);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(140, 23);
            this.button3.TabIndex = 6;
            this.button3.Text = "Start Sending Messages";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // button4
            // 
            this.button4.Enabled = false;
            this.button4.Location = new System.Drawing.Point(341, 99);
            this.button4.Name = "button4";
            this.button4.Size = new System.Drawing.Size(140, 23);
            this.button4.TabIndex = 7;
            this.button4.Text = "Stop Sending Messages";
            this.button4.UseVisualStyleBackColor = true;
            this.button4.Click += new System.EventHandler(this.button4_Click);
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(33, 143);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(48, 13);
            this.label4.TabIndex = 8;
            this.label4.Text = "Bytes In:";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(33, 167);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(56, 13);
            this.label5.TabIndex = 9;
            this.label5.Text = "Bytes Out:";
            // 
            // labelBytesIn
            // 
            this.labelBytesIn.AutoSize = true;
            this.labelBytesIn.Location = new System.Drawing.Point(111, 143);
            this.labelBytesIn.Name = "labelBytesIn";
            this.labelBytesIn.Size = new System.Drawing.Size(13, 13);
            this.labelBytesIn.TabIndex = 10;
            this.labelBytesIn.Text = "0";
            // 
            // labelBytesOut
            // 
            this.labelBytesOut.AutoSize = true;
            this.labelBytesOut.Location = new System.Drawing.Point(111, 167);
            this.labelBytesOut.Name = "labelBytesOut";
            this.labelBytesOut.Size = new System.Drawing.Size(13, 13);
            this.labelBytesOut.TabIndex = 11;
            this.labelBytesOut.Text = "0";
            // 
            // textBoxRemoteHostIP
            // 
            this.textBoxRemoteHostIP.Location = new System.Drawing.Point(129, 32);
            this.textBoxRemoteHostIP.Name = "textBoxRemoteHostIP";
            this.textBoxRemoteHostIP.Size = new System.Drawing.Size(190, 20);
            this.textBoxRemoteHostIP.TabIndex = 12;
            this.textBoxRemoteHostIP.Text = "127.0.0.1";
            // 
            // textBoxRemoteHostPort
            // 
            this.textBoxRemoteHostPort.Location = new System.Drawing.Point(129, 58);
            this.textBoxRemoteHostPort.Name = "textBoxRemoteHostPort";
            this.textBoxRemoteHostPort.Size = new System.Drawing.Size(190, 20);
            this.textBoxRemoteHostPort.TabIndex = 13;
            this.textBoxRemoteHostPort.Text = "5000";
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
            this.ClientSize = new System.Drawing.Size(501, 205);
            this.Controls.Add(this.textBoxRemoteHostPort);
            this.Controls.Add(this.textBoxRemoteHostIP);
            this.Controls.Add(this.labelBytesOut);
            this.Controls.Add(this.labelBytesIn);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.button4);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Name = "Form1";
            this.Text = "Extasys Example TCP Client";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Button button4;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label labelBytesIn;
        private System.Windows.Forms.Label labelBytesOut;
        private System.Windows.Forms.TextBox textBoxRemoteHostIP;
        private System.Windows.Forms.TextBox textBoxRemoteHostPort;
        private System.Windows.Forms.Timer timer1;
    }
}

