# == Schema Information
#
# Table name: compensations
#
#  id         :integer          not null, primary key
#  field      :string(255)
#  value      :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  type       :string(255)
#

class Compensation < ActiveRecord::Base
  extend JSONLoadable
  
  attr_accessible :field, :value
  validates :field, presence: true, uniqueness: true
  validates :value, presence: true, uniqueness: true
end
