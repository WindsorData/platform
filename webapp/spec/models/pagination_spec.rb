require 'spec_helper'

describe Pagination do
  extend Pagination

  it 'should add a default pagination' do
    Company.create({address: 'Evergreen', contact_email: 'foo@windsor.com', name: 'WIND'})
    Company.order.paginated({page: 1}).size.should == 1
    Company.order.paginated({page: 2}).size.should == 0
  end
end