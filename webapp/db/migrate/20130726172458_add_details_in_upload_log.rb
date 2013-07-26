class AddDetailsInUploadLog < ActiveRecord::Migration
  def change
  	create_table :detail_upload_files do |t|
      t.text :file_name
      t.integer :ticker_id
      t.integer :upload_log_id
      t.text :messages
    end

    add_index :detail_upload_files, :ticker_id
    add_index :detail_upload_files, :upload_log_id

  end
end
