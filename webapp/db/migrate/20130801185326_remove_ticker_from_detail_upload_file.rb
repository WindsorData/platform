class RemoveTickerFromDetailUploadFile < ActiveRecord::Migration
  def up
    remove_column :detail_upload_files, :ticker_id
  end

  def down
    add_column :detail_upload_files, :ticker_id, :number
  end
end
