# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  # config.vm.box = "bento/ubuntu-16.04"
  config.vm.box = "ubuntu/wily64"
  config.vm.box_check_update = true

  config.vm.hostname = "raptor.local"
  config.vm.network "private_network", ip: "192.168.100.10", auto_config: true

  config.vm.provider "virtualbox" do |vb|
    # vb.memory = 1024 * 6 # 6GB
    vb.memory = 1024 * 4
  end

  # config.vm.provision "ansible" do |ansible|
  #   ansible.playbook = "ansible/playbook.yml"
  # end

  config.vm.provision "shell", path: "provision.sh"

end
