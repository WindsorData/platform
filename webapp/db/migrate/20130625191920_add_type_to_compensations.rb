class AddTypeToCompensations < ActiveRecord::Migration
  def change
    add_column :compensations, :type, :string
  end
end
