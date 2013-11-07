class AddGroupNameToSearch < ActiveRecord::Migration
  def change
    add_column :searches, :group_name, :string
  end
end
