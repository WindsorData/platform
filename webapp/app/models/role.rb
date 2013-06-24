# == Schema Information
#
# Table name: roles
#
#  id         :integer          not null, primary key
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

require 'json_loadable.rb'
class Role < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :name
  validates :name, presence: true
end
