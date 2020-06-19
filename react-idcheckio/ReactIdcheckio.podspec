Pod::Spec.new do |s|
  s.name         = "ReactIdcheckio"
  s.version      = "5.4.3"
  s.summary      = "React Native plugin for IDCheck.io Mobile SDK for iOS"
  s.homepage     = "https://www.github.com//react-idcheckio"
  s.license      = { :type => "ISC" }
  s.authors      = { "" => "" }
  s.platform     = :ios, "10.0"
  s.source       = { :path => "." }
  s.source_files = "ios", "ios/**/*.{h,m,swift}"
  s.swift_version = '5.2.2'

  s.dependency 'React'
  s.dependency 'IDCheckIOSDK', '5.4.3-legacy'
end
