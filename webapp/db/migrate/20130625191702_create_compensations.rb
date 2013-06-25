class CreateCompensations < ActiveRecord::Migration
  def change
    create_table :compensations do |t|
      t.string :field
      t.string :value
      t.timestamps
    end
  end
end
