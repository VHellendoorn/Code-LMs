namespace FFBatch
{
    partial class Form15
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form15));
            System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle1 = new System.Windows.Forms.DataGridViewCellStyle();
            System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle2 = new System.Windows.Forms.DataGridViewCellStyle();
            this.dg_pr = new System.Windows.Forms.DataGridView();
            this.Column1 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Column2 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Column3 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.btn_save = new System.Windows.Forms.Button();
            this.btn_load = new System.Windows.Forms.Button();
            this.open_file = new System.Windows.Forms.OpenFileDialog();
            this.btn_cancel = new System.Windows.Forms.Button();
            this.btn_load_bck = new System.Windows.Forms.Button();
            this.btn_decr_font = new System.Windows.Forms.Button();
            this.btn_inc_font = new System.Windows.Forms.Button();
            this.item_down = new System.Windows.Forms.Button();
            this.item_up = new System.Windows.Forms.Button();
            this.btn_remove_pr = new System.Windows.Forms.Button();
            this.btn_add_pr = new System.Windows.Forms.Button();
            this.txt_config_ver = new System.Windows.Forms.TextBox();
            this.lbl_config = new System.Windows.Forms.Label();
            this.btn_save_backup = new System.Windows.Forms.Button();
            this.btn_clear = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.dg_pr)).BeginInit();
            this.SuspendLayout();
            // 
            // dg_pr
            // 
            resources.ApplyResources(this.dg_pr, "dg_pr");
            this.dg_pr.AllowDrop = true;
            this.dg_pr.AllowUserToAddRows = false;
            dataGridViewCellStyle1.BackColor = System.Drawing.Color.AliceBlue;
            this.dg_pr.AlternatingRowsDefaultCellStyle = dataGridViewCellStyle1;
            this.dg_pr.BackgroundColor = System.Drawing.SystemColors.InactiveBorder;
            this.dg_pr.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dg_pr.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.Column1,
            this.Column2,
            this.Column3});
            this.dg_pr.Name = "dg_pr";
            this.dg_pr.CellValidating += new System.Windows.Forms.DataGridViewCellValidatingEventHandler(this.dg_pr_CellValidating);
            this.dg_pr.DragDrop += new System.Windows.Forms.DragEventHandler(this.dg_pr_DragDrop);
            this.dg_pr.DragOver += new System.Windows.Forms.DragEventHandler(this.dg_pr_DragOver);
            this.dg_pr.MouseDown += new System.Windows.Forms.MouseEventHandler(this.dg_pr_MouseDown);
            this.dg_pr.MouseMove += new System.Windows.Forms.MouseEventHandler(this.dg_pr_MouseMove);
            // 
            // Column1
            // 
            resources.ApplyResources(this.Column1, "Column1");
            this.Column1.Name = "Column1";
            // 
            // Column2
            // 
            resources.ApplyResources(this.Column2, "Column2");
            this.Column2.Name = "Column2";
            // 
            // Column3
            // 
            dataGridViewCellStyle2.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
            this.Column3.DefaultCellStyle = dataGridViewCellStyle2;
            resources.ApplyResources(this.Column3, "Column3");
            this.Column3.Name = "Column3";
            // 
            // btn_save
            // 
            resources.ApplyResources(this.btn_save, "btn_save");
            this.btn_save.BackColor = System.Drawing.SystemColors.InactiveBorder;
            this.btn_save.FlatAppearance.BorderSize = 0;
            this.btn_save.Name = "btn_save";
            this.btn_save.UseVisualStyleBackColor = false;
            this.btn_save.Click += new System.EventHandler(this.btn_save_Click);
            // 
            // btn_load
            // 
            resources.ApplyResources(this.btn_load, "btn_load");
            this.btn_load.FlatAppearance.BorderSize = 0;
            this.btn_load.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_load.Name = "btn_load";
            this.btn_load.UseVisualStyleBackColor = true;
            this.btn_load.Click += new System.EventHandler(this.button4_Click);
            // 
            // open_file
            // 
            resources.ApplyResources(this.open_file, "open_file");
            this.open_file.FileOk += new System.ComponentModel.CancelEventHandler(this.open_file_FileOk);
            // 
            // btn_cancel
            // 
            resources.ApplyResources(this.btn_cancel, "btn_cancel");
            this.btn_cancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btn_cancel.FlatAppearance.BorderSize = 0;
            this.btn_cancel.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_cancel.Name = "btn_cancel";
            this.btn_cancel.UseVisualStyleBackColor = true;
            this.btn_cancel.Click += new System.EventHandler(this.btn_cancel_Click);
            // 
            // btn_load_bck
            // 
            resources.ApplyResources(this.btn_load_bck, "btn_load_bck");
            this.btn_load_bck.FlatAppearance.BorderSize = 0;
            this.btn_load_bck.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_load_bck.Name = "btn_load_bck";
            this.btn_load_bck.UseVisualStyleBackColor = true;
            this.btn_load_bck.Click += new System.EventHandler(this.btn_load_bck_Click);
            // 
            // btn_decr_font
            // 
            resources.ApplyResources(this.btn_decr_font, "btn_decr_font");
            this.btn_decr_font.FlatAppearance.BorderSize = 0;
            this.btn_decr_font.Name = "btn_decr_font";
            this.btn_decr_font.UseVisualStyleBackColor = true;
            this.btn_decr_font.Click += new System.EventHandler(this.btn_decr_font_Click);
            // 
            // btn_inc_font
            // 
            resources.ApplyResources(this.btn_inc_font, "btn_inc_font");
            this.btn_inc_font.FlatAppearance.BorderSize = 0;
            this.btn_inc_font.Name = "btn_inc_font";
            this.btn_inc_font.UseVisualStyleBackColor = true;
            this.btn_inc_font.Click += new System.EventHandler(this.btn_inc_font_Click);
            // 
            // item_down
            // 
            resources.ApplyResources(this.item_down, "item_down");
            this.item_down.FlatAppearance.BorderSize = 0;
            this.item_down.Name = "item_down";
            this.item_down.UseVisualStyleBackColor = true;
            this.item_down.Click += new System.EventHandler(this.item_down_Click);
            // 
            // item_up
            // 
            resources.ApplyResources(this.item_up, "item_up");
            this.item_up.FlatAppearance.BorderSize = 0;
            this.item_up.Name = "item_up";
            this.item_up.UseVisualStyleBackColor = true;
            this.item_up.Click += new System.EventHandler(this.item_up_Click);
            // 
            // btn_remove_pr
            // 
            resources.ApplyResources(this.btn_remove_pr, "btn_remove_pr");
            this.btn_remove_pr.FlatAppearance.BorderSize = 0;
            this.btn_remove_pr.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_remove_pr.Name = "btn_remove_pr";
            this.btn_remove_pr.UseVisualStyleBackColor = true;
            this.btn_remove_pr.Click += new System.EventHandler(this.btn_remove_pr_Click);
            // 
            // btn_add_pr
            // 
            resources.ApplyResources(this.btn_add_pr, "btn_add_pr");
            this.btn_add_pr.FlatAppearance.BorderSize = 0;
            this.btn_add_pr.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_add_pr.Name = "btn_add_pr";
            this.btn_add_pr.UseVisualStyleBackColor = true;
            this.btn_add_pr.Click += new System.EventHandler(this.btn_add_pr_Click);
            // 
            // txt_config_ver
            // 
            resources.ApplyResources(this.txt_config_ver, "txt_config_ver");
            this.txt_config_ver.BackColor = System.Drawing.SystemColors.Window;
            this.txt_config_ver.Name = "txt_config_ver";
            // 
            // lbl_config
            // 
            resources.ApplyResources(this.lbl_config, "lbl_config");
            this.lbl_config.Name = "lbl_config";
            // 
            // btn_save_backup
            // 
            resources.ApplyResources(this.btn_save_backup, "btn_save_backup");
            this.btn_save_backup.FlatAppearance.BorderSize = 0;
            this.btn_save_backup.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.GradientInactiveCaption;
            this.btn_save_backup.Name = "btn_save_backup";
            this.btn_save_backup.UseVisualStyleBackColor = true;
            this.btn_save_backup.Click += new System.EventHandler(this.btn_save_backup_Click);
            // 
            // btn_clear
            // 
            resources.ApplyResources(this.btn_clear, "btn_clear");
            this.btn_clear.FlatAppearance.BorderSize = 0;
            this.btn_clear.Name = "btn_clear";
            this.btn_clear.UseVisualStyleBackColor = true;
            this.btn_clear.Click += new System.EventHandler(this.btn_clear_Click);
            // 
            // Form15
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.InactiveBorder;
            this.Controls.Add(this.btn_clear);
            this.Controls.Add(this.btn_save_backup);
            this.Controls.Add(this.txt_config_ver);
            this.Controls.Add(this.lbl_config);
            this.Controls.Add(this.btn_add_pr);
            this.Controls.Add(this.btn_remove_pr);
            this.Controls.Add(this.btn_decr_font);
            this.Controls.Add(this.btn_inc_font);
            this.Controls.Add(this.item_down);
            this.Controls.Add(this.item_up);
            this.Controls.Add(this.btn_load_bck);
            this.Controls.Add(this.btn_cancel);
            this.Controls.Add(this.btn_load);
            this.Controls.Add(this.btn_save);
            this.Controls.Add(this.dg_pr);
            this.Name = "Form15";
            this.Load += new System.EventHandler(this.Form15_Load);
            this.Resize += new System.EventHandler(this.Form15_Resize);
            ((System.ComponentModel.ISupportInitialize)(this.dg_pr)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.DataGridView dg_pr;
        private System.Windows.Forms.Button btn_save;
        private System.Windows.Forms.Button btn_load;
        private System.Windows.Forms.OpenFileDialog open_file;
        private System.Windows.Forms.Button btn_cancel;
        private System.Windows.Forms.Button btn_load_bck;
        private System.Windows.Forms.Button btn_decr_font;
        private System.Windows.Forms.Button btn_inc_font;
        private System.Windows.Forms.Button item_down;
        private System.Windows.Forms.Button item_up;
        private System.Windows.Forms.Button btn_remove_pr;
        private System.Windows.Forms.Button btn_add_pr;
        private System.Windows.Forms.TextBox txt_config_ver;
        private System.Windows.Forms.Label lbl_config;
        private System.Windows.Forms.Button btn_save_backup;
        private System.Windows.Forms.Button btn_clear;
        private System.Windows.Forms.DataGridViewTextBoxColumn Column1;
        private System.Windows.Forms.DataGridViewTextBoxColumn Column2;
        private System.Windows.Forms.DataGridViewTextBoxColumn Column3;
    }
}