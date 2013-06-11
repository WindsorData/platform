require 'spec_helper'

describe Company do
  subject { company }
  
  context "When it doesn't have a name" do
    let(:company){ build(:company, name: nil) }
    
    it { should_not be_valid }
  end
  context "When it doesn't have an address" do
    let(:company){ build(:company, address: nil) }
    
    it { should_not be_valid }
  end
  context "When it doesn't have a contact email" do
    let(:company){ build(:company, contact_email: nil) }
    
    it { should_not be_valid }
  end
  context "When exists another company with the same name" do
    let(:other_company){ create(:company) }
    let(:company){ build(:company, name: other_company.name) }
    
    it { should_not be_valid }
  end
  context "When it has a name, contact email and address" do
    let(:company){ build(:company) }
    
    it { should be_valid }
  end
end