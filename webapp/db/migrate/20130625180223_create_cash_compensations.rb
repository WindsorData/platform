class CreateCashCompensations < ActiveRecord::Migration
  def change
    create_table :cash_compensations do |t|
      t.string :field
      t.string :value
      t.timestamps
    end
  end
end
