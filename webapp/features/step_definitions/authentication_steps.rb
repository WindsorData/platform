Given(/^I am a "(.*?)" user$/) do |user_role|
  @user = create(user_role)
end

Given(/^I am logged in$/) do
  step "I log in"
end

When(/^I log in$/) do
  visit new_user_session_path
  fill_in "user_email", with: @user.email
  fill_in "user_password", with: @user.password
  click_button "Sign in"
end
