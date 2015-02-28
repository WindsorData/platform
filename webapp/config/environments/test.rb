WindosorFrontend::Application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  # The test environment is used exclusively to run your application's
  # test suite. You never need to work with it otherwise. Remember that
  # your test database is "scratch space" for the test suite and is wiped
  # and recreated between test runs. Don't rely on the data there!
  config.cache_classes = true

  # Configure static asset server for tests with Cache-Control for performance
  config.serve_static_assets = true
  config.static_cache_control = "public, max-age=3600"

  # Log error messages when you accidentally call methods on nil
  config.whiny_nils = true

  # Show full error reports and disable caching
  config.consider_all_requests_local       = true
  config.action_controller.perform_caching = false

  # Raise exceptions instead of rendering exception templates
  config.action_dispatch.show_exceptions = false

  # Disable request forgery protection in test environment
  config.action_controller.allow_forgery_protection    = false

  # Tell Action Mailer not to deliver emails to the real world.
  # The :test delivery method accumulates sent emails in the
  # ActionMailer::Base.deliveries array.
  config.action_mailer.delivery_method = :test

  # Raise exception on mass assignment protection for Active Record models
  config.active_record.mass_assignment_sanitizer = :strict

  # Print deprecation notices to the stderr
  config.active_support.deprecation = :stderr

  # Backend params
  config.backend_host = 'http://192.168.161.176:9000'
  config.get_tickers_path = '/api/companies'
  config.get_primary_roles_path = '/api/schema/values/roles/primary'
  config.get_secondary_roles_path = '/api/schema/values/roles/secondary'  
  config.get_cash_compensations_path = '/api/schema/values/cashCompensations'
  config.get_equity_compensations_path = '/api/schema/values/equityCompensations'
  config.get_level_roles_path = '/api/schema/values/roles/level'
  config.get_scope_roles_path = '/api/schema/values/roles/scope'
  config.get_bod_roles_path = '/api/schema/values/roles/bod'
  
  config.post_top5_path = '/api/companies/top5'
  config.post_guidelines_path = '/api/companies/guidelines'
  config.post_dilution_path = '/api/companies/dilution'
  config.peers_path = '/api/companies/peers'
  config.post_bod_path = '/api/companies/bod'
  config.post_batch_companies_path = '/api/companies/batch/companies'
  config.post_batch_bod_path = '/api/companies/batch/bod'
  config.post_batch_peers_path = '/api/companies/batch/peers'
  config.post_company_index_path = '/api/companies/indexes'


  # Search paths
  config.post_query_path = '/api/companies/search'
  config.post_download_report_path = '/api/companies/report'
  config.post_download_full_report_path = '/api/companies/report/full'
end
