async function requestDeleteProduct(element) {
    const id = element.parentNode.querySelector("input").value;

    const res = await fetch(`/api/products/${id}`, {
        "method": "DELETE",
        "headers": {
            "Content-Type": "application/json"
        }
    });

    if (res.ok) {
        window.location.reload();
    } else {
        const errorData = await res.json();
        console.error("상품 삭제 요청 실패:", errorData);
        alert(`상품 삭제에 실패했습니다: ${errorData.message}`);
    }
}