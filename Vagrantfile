# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "ubuntu/xenial64"
  #config.vm.box_check_update = true

  config.vm.hostname = "raptor.local"
  config.vm.network "private_network", ip: "192.168.44.55"

  config.vm.provider "virtualbox" do |vb|
    vb.memory = 1026 * 6 # 6GB
  end

  # config.vm.provision "ansible" do |ansible|
  #   ansible.playbook = "ansible/playbook.yml"
  # end


end
