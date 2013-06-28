class AddTypeToRoles < ActiveRecord::Migration
  def change
    add_column :roles, :type, :string
  end
end
