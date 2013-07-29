# == Schema Information
#
# Table name: tickers
#
#  id         :integer          not null, primary key
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  ticker     :string(255)
#  cusip      :string(255)
#

class Ticker < ActiveRecord::Base

  attr_accessible :name, :ticker, :cusip
  has_and_belongs_to_many :groups
  validates :ticker, presence: true, uniqueness: true
  validates :cusip, presence: true, uniqueness: true

  scope :containing_chars, lambda { |s| where("ticker ilike ?", "%#{s}%") }

  def self.load_json(json)
    JSON.parse(json).each{|attrs|
      if t = find_by_cusip(attrs["cusip"])
        t.update_attributes(attrs)
      else
        create(attrs)
      end
    }
  end
end