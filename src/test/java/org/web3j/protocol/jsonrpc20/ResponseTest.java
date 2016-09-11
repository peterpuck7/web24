package org.web3j.protocol.jsonrpc20;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.web3j.Web3jService;
import org.web3j.methods.response.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Protocol tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResponseTest {

    private Web3jService web3jService;

    private CloseableHttpClient closeableHttpClient;
    private CloseableHttpResponse httpResponse;
    private HttpEntity entity;

    @Before
    public void setUp() {
        closeableHttpClient = mock(CloseableHttpClient.class);
        web3jService = new Web3jService("", closeableHttpClient);

        httpResponse = mock(CloseableHttpResponse.class);
        entity = mock(HttpEntity.class);

        when(httpResponse.getStatusLine()).thenReturn(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Test")
        );
        when(httpResponse.getEntity()).thenReturn(entity);
    }

    private <T> T deserialiseResponse(Class<T> type) {
        T response = null;
        try {
            response = web3jService.getResponseHandler(type).handleResponse(httpResponse);
            when(closeableHttpClient.execute(isA(HttpPost.class), isA(ResponseHandler.class))).thenReturn(response);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return response;
    }

    private void buildResponse(String data) {
        try {
            when(entity.getContent()).thenReturn(buildInputStream(data));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private InputStream buildInputStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    @Test
    public void testWeb3ClientVersion() {
        buildResponse(
                "{\n" +
                        "  \"id\":67,\n" +
                        "  \"jsonrpc\":\"2.0\",\n" +
                        "  \"result\": \"Mist/v0.9.3/darwin/go1.4.1\"\n" +
                        "}"
        );

        Web3ClientVersion web3ClientVersion = deserialiseResponse(Web3ClientVersion.class);
        assertThat(web3ClientVersion.getWeb3ClientVersion(), is("Mist/v0.9.3/darwin/go1.4.1"));
    }

    @Test
    public void testWeb3Sha3() throws IOException {
        buildResponse(
                "{\n" +
                        "  \"id\":64,\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": \"0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad\"\n" +
                        "}"
        );

        Web3Sha3 web3Sha3 = deserialiseResponse(Web3Sha3.class);
        assertThat(web3Sha3.getResult(),
                is("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    public void testNetVersion() throws IOException {
        buildResponse(
                "{\n" +
                        "  \"id\":67,\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": \"59\"\n" +
                        "}"
        );

        NetVersion netVersion = deserialiseResponse(NetVersion.class);
        assertThat(netVersion.getNetVersion(), is("59"));
    }

    @Test
    public void testNetListening() throws IOException {
        buildResponse(
                "{\n" +
                        "  \"id\":67,\n" +
                        "  \"jsonrpc\":\"2.0\",\n" +
                        "  \"result\":true\n" +
                        "}"
        );

        NetListening netListening = deserialiseResponse(NetListening.class);
        assertThat(netListening.isListening(), is(true));
    }

    @Test
    public void testNetPeerCount() throws IOException {
        buildResponse(
                "{\n" +
                        "  \"id\":74,\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": \"0x2\"\n" +
                        "}"
        );

        NetPeerCount netPeerCount = deserialiseResponse(NetPeerCount.class);
        assertThat(netPeerCount.getQuantity(), equalTo(BigInteger.valueOf(2L)));
    }

    @Test
    public void testEthProtocolVersion() throws IOException {
        buildResponse(
                "{\n" +
                        "  \"id\":67,\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": \"54\"\n" +
                        "}"
        );

        EthProtocolVersion ethProtocolVersion = deserialiseResponse(EthProtocolVersion.class);
        assertThat(ethProtocolVersion.getProtocolVersion(), is("54"));
    }

    @Test
    public void testEthSyncingInProgress() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": {\n" +
                "  \"startingBlock\": \"0x384\",\n" +
                "  \"currentBlock\": \"0x386\",\n" +
                "  \"highestBlock\": \"0x454\"\n" +
                "  }\n" +
                "}"
        );

        // Response received from Geth node
        // "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"currentBlock\":\"0x117a\",\"highestBlock\":\"0x21dab4\",\"knownStates\":\"0x0\",\"pulledStates\":\"0x0\",\"startingBlock\":\"0xa51\"}}"

        EthSyncing ethSyncing = deserialiseResponse(EthSyncing.class);

        assertThat(ethSyncing.getResult(), equalTo(new EthSyncing.Syncing("0x384", "0x386", "0x454", null, null)));
    }

    @Test
    public void testEthSyncing() {
        buildResponse(
                "{\n" +
                        "  \"id\":1,\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": false\n" +
                        "}"
        );

        EthSyncing ethSyncing = deserialiseResponse(EthSyncing.class);
        assertThat(ethSyncing.isSyncing(), is(false));
    }

    @Test
    public void testEthMining() {
        buildResponse(
                "{\n" +
                "  \"id\":71,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": true\n" +
                "}"
        );

        EthMining ethMining = deserialiseResponse(EthMining.class);
        assertThat(ethMining.isMining(), is(true));
    }

    @Test
    public void testEthHashrate() {
        buildResponse(
                "{\n" +
                "  \"id\":71,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x38a\"\n" +
                "}"
        );

        EthHashrate ethHashrate = deserialiseResponse(EthHashrate.class);
        assertThat(ethHashrate.getHashrate(), equalTo(BigInteger.valueOf(906L)));
    }

    @Test
    public void testEthGasPrice() {
        buildResponse(
                "{\n" +
                "  \"id\":73,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x9184e72a000\"\n" +
                "}"
        );

        EthGasPrice ethGasPrice = deserialiseResponse(EthGasPrice.class);
        assertThat(ethGasPrice.getGasPrice(), equalTo(BigInteger.valueOf(10000000000000L)));
    }

    @Test
    public void testEthAccounts() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": [\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]\n" +
                "}"
        );

        EthAccounts ethAccounts = deserialiseResponse(EthAccounts.class);
        assertThat(ethAccounts.getAccounts(), equalTo(Arrays.asList("0x407d73d8a49eeb85d32cf465507dd71d507100c1")));
    }

    @Test
    public void testEthBlockNumber() {
        buildResponse(
                "{\n" +
                "  \"id\":83,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x4b7\"\n" +
                "}"
        );

        EthBlockNumber ethBlockNumber = deserialiseResponse(EthBlockNumber.class);
        assertThat(ethBlockNumber.getBlockNumber(), equalTo(BigInteger.valueOf(1207L)));
    }

    @Test
    public void testEthGetBalance() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x234c8a3397aab58\"\n" +
                "}"
        );

        EthGetBalance ethGetBalance = deserialiseResponse(EthGetBalance.class);
        assertThat(ethGetBalance.getBalance(), equalTo(BigInteger.valueOf(158972490234375000L)));
    }

    @Test
    public void testEthStorageAt() {
        buildResponse(
                "{" +
                "    \"jsonrpc\":\"2.0\"," +
                "    \"id\":1," +
                "    \"result\":\"0x000000000000000000000000000000000000000000000000000000000000162e\"" +
                "}"
        );

        EthGetStorageAt ethGetStorageAt = deserialiseResponse(EthGetStorageAt.class);
        assertThat(ethGetStorageAt.getResult(), is("0x000000000000000000000000000000000000000000000000000000000000162e"));
    }

    @Test
    public void testEthGetTransactionCount() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x1\"\n" +
                "}"
        );

        EthGetTransactionCount ethGetTransactionCount = deserialiseResponse((EthGetTransactionCount.class));
        assertThat(ethGetTransactionCount.getTransactionCount(), equalTo(BigInteger.valueOf(1L)));
    }

    @Test
    public void testEthGetTransactionCountByHash() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0xb\"\n" +
                "}"
        );

        EthGetTransactionCountByHash ethGetTransactionCountByHash = deserialiseResponse(
                EthGetTransactionCountByHash.class);
        assertThat(ethGetTransactionCountByHash.getTransactionCount(),
                equalTo(BigInteger.valueOf(11)));
    }

    @Test
    public void testEthGetBlockTransactionCountByNumber() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0xa\"\n" +
                "}"
        );

        EthGetBlockTransactionCountByNumber ethGetBlockTransactionCountByNumber = deserialiseResponse(
                EthGetBlockTransactionCountByNumber.class);
        assertThat(ethGetBlockTransactionCountByNumber.getTransactionCount(),
                equalTo(BigInteger.valueOf(10)));
    }

    @Test
    public void testEthGetUncleCountByBlockHash() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x1\"\n" +
                "}"
        );

        EthGetUncleCountByBlockHash ethGetUncleCountByBlockHash = deserialiseResponse(
                EthGetUncleCountByBlockHash.class);
        assertThat(ethGetUncleCountByBlockHash.getUncleCount(),
                equalTo(BigInteger.valueOf(1)));
    }

    @Test
    public void testEthGetUncleCountByBlockNumber() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x1\"\n" +
                "}"
        );

        EthGetUncleCountByBlockNumber ethGetUncleCountByBlockNumber = deserialiseResponse(
                EthGetUncleCountByBlockNumber.class);
        assertThat(ethGetUncleCountByBlockNumber.getUncleCount(),
                equalTo(BigInteger.valueOf(1)));
    }

    @Test
    public void testGetCode() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056\"\n" +
                "}"
        );

        EthGetCode ethGetCode = deserialiseResponse(EthGetCode.class);
        assertThat(ethGetCode.getCode(), is("0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056"));
    }

    @Test
    public void testEthSign() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001\"\n" +
                "}"
        );

        EthSign ethSign = deserialiseResponse(EthSign.class);
        assertThat(ethSign.getSignature(), is("0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001"));
    }

    @Test
    public void testEthSendTransaction() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n" +
                "}"
        );

        EthSendTransaction ethSendTransaction = deserialiseResponse(EthSendTransaction.class);
        assertThat(ethSendTransaction.getTransactionHash(), is("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    public void testEthSendRawTransaction() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n" +
                "}"
        );

        EthSendRawTransaction ethSendRawTransaction = deserialiseResponse(EthSendRawTransaction.class);
        assertThat(ethSendRawTransaction.getTransactionHash(), is("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    public void testEthCall() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x\"\n" +
                "}"
        );

        EthCall ethCall = deserialiseResponse(EthCall.class);
        assertThat(ethCall.getValue(), is("0x"));
    }

    @Test
    public void testEthEstimateGas() {
        buildResponse(
                "{\n" +
                "  \"id\":1,\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"result\": \"0x5208\"\n" +
                "}"
        );

        EthEstimateGas ethEstimateGas = deserialiseResponse(EthEstimateGas.class);
        assertThat(ethEstimateGas.getAmountUsed(), equalTo(BigInteger.valueOf(21000)));
    }

    @Test
    public void testEthBlockTransactionHashes() {
        buildResponse(
                "{\n" +
                "\"id\":1,\n" +
                "\"jsonrpc\":\"2.0\",\n" +
                "\"result\": {\n" +
                "    \"number\": \"0x1b4\",\n" +
                "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n" +
                "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n" +
                "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n" +
                "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n" +
                "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n" +
                "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n" +
                "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n" +
                "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n" +
                "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n" +
                "    \"difficulty\": \"0x027f07\",\n" +
                "    \"totalDifficulty\":  \"0x027f07\",\n" +
                "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n" +
                "    \"size\":  \"0x027f07\",\n" +
                "    \"gasLimit\": \"0x9f759\",\n" +
                "    \"gasUsed\": \"0x9f759\",\n" +
                "    \"timestamp\": \"0x54e34e8e\",\n" +
                "    \"transactions\": [" +
                "        \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n" +
                "        \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1df\"\n" +
                "    ], \n" +
                "    \"uncles\": [\n" +
                "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n" +
                "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        );

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        EthBlock.Block block = new EthBlock.Block(
                "0x1b4",
                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                "0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5",
                "0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2",
                "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff",
                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                "0x4e65fda2159562a496f9f3522f89122a3088497a",
                "0x027f07",
                "0x027f07",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "0x027f07",
                "0x9f759",
                "0x9f759",
                "0x54e34e8e",
                Arrays.asList(
                        new EthBlock.TransactionHash("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"),
                        new EthBlock.TransactionHash("0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1df")
                ),
                Arrays.asList(
                        "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff"
                )
        );
        assertThat(ethBlock.getBlock().get(),
                equalTo(block));
    }

    @Test
    public void testEthBlockFullTransactions() {
        buildResponse(
                "{\n" +
                        "\"id\":1,\n" +
                        "\"jsonrpc\":\"2.0\",\n" +
                        "\"result\": {\n" +
                        "    \"number\": \"0x1b4\",\n" +
                        "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n" +
                        "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n" +
                        "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n" +
                        "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n" +
                        "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n" +
                        "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n" +
                        "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n" +
                        "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n" +
                        "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n" +
                        "    \"difficulty\": \"0x027f07\",\n" +
                        "    \"totalDifficulty\":  \"0x027f07\",\n" +
                        "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n" +
                        "    \"size\":  \"0x027f07\",\n" +
                        "    \"gasLimit\": \"0x9f759\",\n" +
                        "    \"gasUsed\": \"0x9f759\",\n" +
                        "    \"timestamp\": \"0x54e34e8e\",\n" +
                        "    \"transactions\": [{" +
                        "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n" +
                        "        \"nonce\":\"0x\",\n" +
                        "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n" +
                        "        \"blockNumber\": \"0x15df\",\n" +
                        "        \"transactionIndex\":  \"0x1\",\n" +
                        "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n" +
                        "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n" +
                        "        \"value\":\"0x7f110\",\n" +
                        "        \"gas\": \"0x7f110\",\n" +
                        "        \"gasPrice\":\"0x09184e72a000\",\n" +
                        "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\"" +
                        "    }], \n" +
                        "    \"uncles\": [\n" +
                        "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n" +
                        "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}"
        );

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        EthBlock.Block block = new EthBlock.Block(
                "0x1b4",
                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                "0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5",
                "0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2",
                "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff",
                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                "0x4e65fda2159562a496f9f3522f89122a3088497a",
                "0x027f07",
                "0x027f07",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "0x027f07",
                "0x9f759",
                "0x9f759",
                "0x54e34e8e",
                Arrays.asList(new EthBlock.TransactionObject(
                            "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                            "0x",
                            "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                            "0x15df",
                            "0x1",
                            "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                            "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                            "0x7f110",
                            "0x7f110",
                            "0x09184e72a000",
                            "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360"
                        )
                ),
                Arrays.asList(
                        "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff"
                )
        );
        assertThat(ethBlock.getBlock().get(),
                equalTo(block));
    }

    @Test
    public void testEthBlockNull() {
        buildResponse(
                "{\n" +
                "  \"result\": null\n" +
                "}"
        );

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        assertThat(ethBlock.getBlock(), is(Optional.empty()));
    }

    @Test
    public void testEthTransaction() {
        buildResponse(
                "{\n" +
                "    \"id\":1,\n" +
                "    \"jsonrpc\":\"2.0\",\n" +
                "    \"result\": {\n" +
                "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n" +
                "        \"nonce\":\"0x\",\n" +
                "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n" +
                "        \"blockNumber\": \"0x15df\",\n" +
                "        \"transactionIndex\":  \"0x1\",\n" +
                "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n" +
                "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n" +
                "        \"value\":\"0x7f110\",\n" +
                "        \"gas\": \"0x7f110\",\n" +
                "        \"gasPrice\":\"0x09184e72a000\",\n" +
                "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\"\n" +
                "  }\n" +
                "}"
        );
        Transaction transaction = new Transaction(
                "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                "0x",
                "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                "0x15df",
                "0x1",
                "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                "0x7f110",
                "0x7f110",
                "0x09184e72a000",
                "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360"
        );

        EthTransaction ethTransaction = deserialiseResponse(EthTransaction.class);
        assertThat(ethTransaction.getTransaction().get(), equalTo(transaction));
    }

    @Test
    public void testEthTransactionNull() {
        buildResponse(
                "{\n" +
                "  \"result\": null\n" +
                "}"
        );

        EthTransaction ethTransaction = deserialiseResponse(EthTransaction.class);
        assertThat(ethTransaction.getTransaction(), is(Optional.empty()));
    }

    @Test
    public void testeEthGetTransactionReceipt() {
        buildResponse(
                "{\n" +
                "    \"id\":1,\n" +
                "    \"jsonrpc\":\"2.0\",\n" +
                "    \"result\": {\n" +
                "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n" +
                "        \"transactionIndex\":  \"0x1\",\n" +
                "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n" +
                "        \"blockNumber\": \"0xb\",\n" +
                "        \"cumulativeGasUsed\": \"0x33bc\",\n" +
                "        \"gasUsed\": \"0x4dc\",\n" +
                "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n" +
                "        \"logs\": [{\n" +
                "            \"removed\": false,\n" +
                "            \"logIndex\": \"0x1\",\n" +
                "            \"transactionIndex\": \"0x0\",\n" +
                "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n" +
                "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n" +
                "            \"blockNumber\":\"0x1b4\",\n" +
                "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n" +
                "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n" +
                "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]" +
                "        }]\n" +
                "  }\n" +
                "}"
        );

        EthGetTransactionReceipt.TransactionReceipt transactionReceipt =
                new EthGetTransactionReceipt.TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "0x33bc",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"
                                        )
                                )
                        )

                );

        EthGetTransactionReceipt ethGetTransactionReceipt = deserialiseResponse(
                EthGetTransactionReceipt.class);
        assertThat(ethGetTransactionReceipt.getTransactionReceipt().get(),
                equalTo(transactionReceipt));
    }

//    public <T> void validate(Class<T> type, ) {
//        T response = deserialiseResponse(type);
//        assertThat(ethGetUncleCountByBlockHash.getUncleCount(),
//                equalTo(BigInteger.valueOf(1)));
//    }
}
