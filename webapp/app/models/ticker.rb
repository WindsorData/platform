# == Schema Information
#
# Table name: tickers
#
#  id         :integer          not null, primary key
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  ticker     :string(255)
#

class Ticker < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :name, :ticker
  has_and_belongs_to_many :groups
  validates :name, presence: true, uniqueness: true
  validates :ticker, presence: true, uniqueness: true

  scope :containing_chars, lambda { |s| where("name ilike ?", "%#{s}%") }
end
