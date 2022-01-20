Pod::Spec.new do |s|
  s.name         = "ReactIdcheckio"
  s.version      = "6.1.0"
  s.summary      = "React Native plugin for IDCheck.io Mobile SDK for iOS"
  s.homepage     = "https://github.com/ariadnext/React-Native-SmartSDK/tree/master/react-idcheckio"
  s.license      = { :type => "ISC" }
  s.authors      = { "" => "" }
  s.platform     = :ios, "10.0"
  s.source       = { :path => "." }
  s.source_files = "ios", "ios/**/*.{h,m,swift}"
  s.swift_version = '5.4.2'

  s.dependency 'React'
  s.dependency 'IDCheckIOSDK', '~> 6.1.0'
end
