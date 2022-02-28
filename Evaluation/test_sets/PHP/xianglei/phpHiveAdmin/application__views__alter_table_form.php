<div class="span10">

	<a href="javascript:history.back()" class="btn btn-inverse"><i class="icon-backward icon-white"></i> <?php echo $common_back;?></a>
	<br><br>
	<form method="post" action="<?php echo $this->config->base_url();?>index.php/table/renametableaction/">
	<table class="table table-bordered">
		<tr class="warning">
			<td><?php echo $common_rename_table;?></td>
			<td><input type="text" name="new_table_name" value="<?php echo $table_name?>" class="input-large search-query"></td>
			<td><input type="submit" value="<?php echo $common_submit;?>" class="btn btn-danger"></td>
		</tr>
	</table>
	<input type="hidden" name="db_name" value="<?php echo $var_db_name;?>">
	<input type="hidden" name="old_table_name" value="<?php echo $table_name;?>">
	</form>
	
	<br>
	
	<form method="post" action="<?php echo $this->config->base_url();?>index.php/table/changeexternalaction/">
	<table  class="table table-bordered">
		<tr>
			<td><?php echo $common_table_type;?></td>
			<td>
				<select name="external">
					<option value="EXTERNAL_TABLE" <?php echo ($external == "EXTERNAL_TABLE") ? "selected" : "";?>><?php echo $common_external;?></option>
					<option value="MANAGED_TABLE" <?php echo ($external == "MANAGED_TABLE") ? "selected" : "";?>><?php echo $common_managed;?></option>
					<option value="INDEX_TABLE" disabled><?php echo $common_index_table;?></option>
					<option value="VIRTUAL_VIEW" disabled><?php echo $common_virtual_view;?></option>
				</select><?php echo $external;?>
			</td>
			<td><input type="submit" value="<?php echo $common_submit;?>" class="btn btn-danger"></td>
		</tr>
	</table>
	<input type="hidden" name="db_name" value="<?php echo $var_db_name;?>">
	<input type="hidden" name="table_name" value="<?php echo $table_name;?>">
	</form>
	
	<br>
	
	<form method="post" action="<?php echo $this->config->base_url();?>index.php/table/altercolumnsaction/">
	<table  class="table table-bordered">
		<tr>
			<td><?php echo $common_column_name;?></td>
			<td><?php echo $common_column_type;?></td>
			<td><?php echo $common_comment;?></td>
			<td></td>
		</tr>
		<?php for($i = 0; $i < count($cols_name); $i++):?>
		<tr>
			<td>
				<input type="text" name="cols_name[]" value="<?php echo $cols_name[$i];?>" />
				<input type="hidden" name="old_cols_name[]" value="<?php echo $cols_name[$i];?>" />
			</td>
			<td>
				<select name="cols_type[]">
				<?php foreach($type as $k => $v):?>
						<option value="<?php echo $k;?>" <?php echo ($k == "map" || $k == 'arrays' || $k == 'structs') ? "disabled" : "";?> <?php echo ($k == $cols_type[$i]) ? "selected" : "";?>><?php echo $v;?></option>
				<?php endforeach;?>
				</select>
			</td>
			<td><input type="text" name="cols_comment[]" value="<?php echo $cols_comment[$i];?>" /></td>
			
			<td><a href="<?php echo $this->config->base_url();?>index.php/table/dropcolumnsaction/<?php echo $var_db_name;?>/<?php echo $table_name;?>/<?php echo $cols_name[$i];?>" class="btn btn-danger"><?php echo $common_delete;?></a></td>
		</tr>
		<?php endfor;?>
	</table>
	<input type="hidden" name="db_name" value="<?php echo $var_db_name;?>" />
	<input type="hidden" name="table_name" value="<?php echo $table_name;?>" />
	<input type="submit" value="<?php echo $common_submit;?>" class="btn btn-danger" />
	</form>
	
	<br>
	
	<form method="post" action="<?php echo $this->config->base_url();?>index.php/table/addcolumns/">
	<table class="table table-bordered">
		<tr class="warning">
			<td><?php echo $common_add_columns;?></td>
			<td><input type="text" name="cols_num" placeholder="<?php echo $common_field_numbers?>" class="input-large"></td>
			<td><input type="submit" value="<?php echo $common_submit;?>" class="btn btn-danger"></td>
		</tr>
	</table>
	<input type="hidden" name="db_name" value="<?php echo $var_db_name;?>">
	<input type="hidden" name="table_name" value="<?php echo $table_name;?>">
	</form>
</div>