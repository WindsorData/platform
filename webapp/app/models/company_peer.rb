# == Schema Information
#
# Table name: company_peers
#
#  id         :integer          not null, primary key
#  ticker     :string(255)      not null
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

class CompanyPeer < ActiveRecord::Base
	extend JSONLoadable
  attr_accessible :name, :ticker
  validates_uniqueness_of :ticker

  scope :containing_chars, lambda { |s| where("ticker ilike ?", "#{s}%") }
end
