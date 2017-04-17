package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.solidity.SolidityCompiler;
import org.adridadou.ethereum.propeller.solidity.converters.SolidityTypeGroup;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.*;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.list.ArrayDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.list.ListDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.list.SetDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.*;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.list.ArrayEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.list.ListEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.list.SetEncoder;
import org.adridadou.ethereum.propeller.swarm.SwarmService;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public final class CoreEthereumFacadeProvider {

    private CoreEthereumFacadeProvider() {
    }

    public static EthereumFacade create(EthereumBackend backend) {
        EthereumEventHandler eventHandler = new EthereumEventHandler();
        SwarmService swarm = new SwarmService(SwarmService.PUBLIC_HOST);
        EthereumProxy proxy = new EthereumProxy(backend, eventHandler);

        registerDefaultEncoders(proxy);
        registerDefaultDecoders(proxy);
        registerDefaultListDecoder(proxy);
        registerDefaultListEncoder(proxy);

        return new EthereumFacade(proxy, swarm, new SolidityCompiler());
    }

    private static void registerDefaultDecoders(EthereumProxy proxy) {
        proxy
                .addDecoder(SolidityTypeGroup.Number, new NumberDecoder())
                .addDecoder(SolidityTypeGroup.Bool, new BooleanDecoder())
                .addDecoder(SolidityTypeGroup.String, new StringDecoder())
                .addDecoder(SolidityTypeGroup.Address, new AddressDecoder())
                .addDecoder(SolidityTypeGroup.Number, new DateDecoder())
                .addDecoder(SolidityTypeGroup.Number, new EnumDecoder());
    }

    private static void registerDefaultEncoders(EthereumProxy proxy) {
        proxy
                .addEncoder(SolidityTypeGroup.Number, new NumberEncoder())
                .addEncoder(SolidityTypeGroup.Bool, new BooleanEncoder())
                .addEncoder(SolidityTypeGroup.String, new StringEncoder())
                .addEncoder(SolidityTypeGroup.Address, new AddressEncoder())
                .addEncoder(SolidityTypeGroup.Address, new AccountEncoder())
                .addEncoder(SolidityTypeGroup.Number, new DateEncoder());
    }


    private static void registerDefaultListDecoder(EthereumProxy proxy) {
        proxy
                .addListDecoder(ListDecoder.class)
                .addListDecoder(SetDecoder.class)
                .addListDecoder(ArrayDecoder.class);
    }

    private static void registerDefaultListEncoder(EthereumProxy proxy) {
        proxy
                .addListEncoder(ListEncoder.class)
                .addListEncoder(SetEncoder.class)
                .addListEncoder(ArrayEncoder.class);
    }
}
