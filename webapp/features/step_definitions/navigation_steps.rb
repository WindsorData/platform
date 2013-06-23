Given(/^I go to "(.*?)"$/) do |page_name|
  visit "#{page_name}"
end

Then(/^I see "(.*?)"$/) do |content|
  page.should have_content(content)
end

Then(/^I should be in "(.*?)" page$/) do |page_name|
  current_path.should eq page_name
end