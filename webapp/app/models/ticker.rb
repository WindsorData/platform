# == Schema Information
#
# Table name: tickers
#
#  id         :integer          not null, primary key
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

class Ticker < ActiveRecord::Base
  attr_accessible :name
  has_and_belongs_to_many :groups

  validates :name, presence: true, uniqueness: true

  scope :containing_chars, lambda { |s| where("name like ?", "%#{s}%") }

  def self.load_tickers(json)
    Ticker.create(JSON.parse(json))
  end
end