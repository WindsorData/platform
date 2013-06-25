# == Schema Information
#
# Table name: cash_compensations
#
#  id         :integer          not null, primary key
#  field      :string(255)
#  value      :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

require 'json_loadable.rb'
class CashCompensation < ActiveRecord::Base
  extend JSONLoadable
  
  attr_accessible :field, :value
  validates :field, presence: true
  validates :value, presence: true
end
