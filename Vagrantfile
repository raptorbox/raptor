# -*- mode: ruby -*-
# vi: set ft=ruby :
Vagrant.configure('2') do |config|
    config.vm.box = 'ubuntu/xenial64'
    config.vm.network 'private_network', ip: '192.168.33.10'
    # explicitly sync https://bugs.launchpad.net/cloud-images/+bug/1565985
    config.vm.synced_folder '.', '/vagrant/'
    config.vm.provider 'virtualbox' do |vb|
        vb.memory = '4096'
    end
    config.vm.provision 'shell', path: 'scripts/provision.sh'
end
