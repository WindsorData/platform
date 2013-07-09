class CreateUploadLogs < ActiveRecord::Migration
  def change
    create_table :upload_logs do |t|
      t.string :user
      t.text :message
      t.string :upload_type

      t.timestamps
    end
  end
end
