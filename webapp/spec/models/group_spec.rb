require 'spec_helper'

describe Group do
  subject { group }
  
  context "When it doesn't have a name" do
    let(:group){ build(:group, name: nil) }

    it { should_not be_valid }
  end
  context "When exists another group with the same name and the same company" do
    let(:company){ create(:company) }
    let(:existent_group){ create(:group, name: "group", company: company) }
    let(:group){ build(:group, name: existent_group.name, company: existent_group.company) }

    it { should_not be_valid }
  end
  context "When exists another group with the same name and different company" do
    let(:company){ create(:company) }
    let(:existent_group){ create(:group, name: "group", company: company) }
    let(:group){ build(:group, name: existent_group.name) }

    it { should be_valid }
  end
  context "When it has a unique name and a company" do
    let(:group){ build(:group) }

    it { should be_valid }
  end
  context "When it has a unique name and no company" do
    let(:group){ build(:group, company: nil) }

    it { should be_valid }
  end  
end