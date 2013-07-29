class RemoveMessageFromUploadLog < ActiveRecord::Migration
  def up
    remove_column :upload_logs, :message
  end

  def down
    add_column :upload_logs, :message, :string
  end
end
