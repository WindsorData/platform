class AddTickerNameFromDetailUploadFile < ActiveRecord::Migration
  def change
    add_column :detail_upload_files, :ticker, :string
    add_index :detail_upload_files, :ticker
  end
end
